/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2018 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.api.callback.AccessEnablerCallback;
import com.adobe.adobepass.accessenabler.api.utils.AccessEnablerConstants;
import com.adobe.adobepass.accessenabler.api.profile.UserProfileService;
import com.adobe.adobepass.accessenabler.models.Event;
import com.adobe.adobepass.accessenabler.models.MetadataKey;
import com.adobe.adobepass.accessenabler.models.MetadataStatus;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.adobe.adobepass.accessenabler.models.decision.PreauthorizeRequest;
import com.adobe.adobepass.accessenabler.models.decision.PreauthorizeResponse;
import com.adobe.adobepass.accessenabler.models.decision.Decision;
import com.adobe.adobepass.accessenabler.utils.Utils;
import com.adobe.adobepass.apps.demo.AccessEnablerDelegate;
import com.adobe.adobepass.apps.demo.AdobePassDemoApp;
import com.adobe.adobepass.apps.demo.R;
import com.adobe.adobepass.apps.demo.ui.storageviewer.StorageViewerActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import static com.adobe.adobepass.apps.demo.AdobePassDemoApp.PRODUCTION_URL;
import static com.adobe.adobepass.apps.demo.ui.PreferencesActivity.ADOBE_PASS_PREFERENCES;
import static com.adobe.adobepass.apps.demo.ui.PreferencesActivity.PREF_SIGNING_CREDENTIAL;

import static com.adobe.adobepass.apps.demo.ui.SetSoftwareStatementActivity.PREF_ENVIRONMENT;
import static com.adobe.adobepass.apps.demo.ui.SetSoftwareStatementActivity.PREF_SOFTWARE_STATEMENT;
import static com.adobe.adobepass.apps.demo.ui.SetSoftwareStatementActivity.PREF_REDIRECT_URL;

public class MainActivity extends AbstractActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final int AE_PERMISSIONS_REQUEST_CODE = 34567;

    private String SP_URL_HARDCODED;
    private int SIGNING_CREDENTIAL_KEY_SIZE;

    private static final int MVPD_PICKER_ACTIVITY = 1;
    private static final int MVPD_LOGIN_ACTIVITY = 2;
    private static final int MVPD_LOGOUT_ACTIVITY = 3;
    private static final int PREFERENCES_ACTIVITY = 4;
    private static final int GENERIC_DATA_ACTIVITY = 5;
    private static final int APP_OPTIONS_ACTIVITY = 6;
    private static final int SET_SOFTWARE_STATEMENT_ACTIVITY = 7;

    private static final String MAIN_ACTIVITY_UI_PREFERENCES = "mainActivityUIPreferences";
    private static final String REQUESTOR_ID_PREF = "requestorIdPref";
    private static final String RESOURCE_ID_PREF = "resourceIdPref";
    private static final String USER_METADATA_PREF = "userMetadataPref";

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int opCode = bundle.getInt("op_code");

            messageHandlers[opCode].handle(bundle);
        }
    };

    private WebView logoutWebView;

    private ProgressDialog spWorkSpinWheel;

    private EditText editRequestorId;
    private EditText editResourceId;
    private EditText editUserMetadata;
    private EditText editSelectedProvider;

    private LinearLayout authnViewGroup;
    private LinearLayout authzViewGroup;
    private LinearLayout userMetadataViewGroup;

    private AccessEnabler accessEnabler;
    private AccessEnablerDelegate delegate = new AccessEnablerDelegate(handler);

    MutableLiveData<Pair<String,String>> messageDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "Creating main activity.");

        // inflate the layout for this activity
        setContentView(R.layout.main);

        // obtain handles to UI elements
        editRequestorId = (EditText) findViewById(R.id.edit_requestor_id);
        Button btnSetRequestor = (Button) findViewById(R.id.btn_set_requestor_id);
        Button btnOptions = (Button) findViewById(R.id.btn_global_options);
        Button btnStorageViewer = (Button) findViewById(R.id.btn_storage_viewer);
        Button btnPreferences = (Button) findViewById(R.id.btn_show_preferences);
        Button btnLogout = (Button) findViewById(R.id.btn_logout);

        Button btnCheckAuthN = (Button) findViewById(R.id.btn_check_authn);
        Button btnGetAuthN = (Button) findViewById(R.id.btn_get_authn);
        Button btnGetAuthNWithData = (Button) findViewById(R.id.btn_get_authn_with_data);
        Button btnTempPassReset = (Button) findViewById(R.id.btn_temp_pass_reset);

        editResourceId = (EditText) findViewById(R.id.edit_resource_id);
        Button btnAuthorize = (Button) findViewById(R.id.btn_authorize);
        Button btnPreauthorize = (Button) findViewById(R.id.btn_preauthorize);
        Button btnCheckAuthZ = (Button) findViewById(R.id.btn_check_authz);
        Button btnCheckAuthZWithData = (Button) findViewById(R.id.btn_check_authz_with_data);
        Button btnGetAuthZ = (Button) findViewById(R.id.btn_get_authz);
        Button btnGetAuthZWithData = (Button) findViewById(R.id.btn_get_authz_with_data);
        Button btnCheckPreAuthZ = (Button) findViewById(R.id.btn_check_preauthz);

        editUserMetadata = (EditText) findViewById(R.id.edit_user_metadata);
        Button btnGetUserMetadata = (Button) findViewById(R.id.btn_get_user_metadata);

        authnViewGroup = (LinearLayout) findViewById(R.id.view_group_authn);
        authzViewGroup = (LinearLayout) findViewById(R.id.view_group_authz);
        userMetadataViewGroup = (LinearLayout) findViewById(R.id.view_group_user_metadata);

        // selected provider group
        Button btnSetSelectedProvider = (Button) findViewById(R.id.btn_set_selected_provider);
        editSelectedProvider = (EditText) findViewById(R.id.edit_selected_provider);

        // install event listeners
        btnSetRequestor.setOnClickListener(btnSetRequestorIdOnClickListener);
        btnOptions.setOnClickListener(btnGlobalOptionsOnClickLister);
        btnStorageViewer.setOnClickListener(btnStorageViewerOnClickListener);
        btnPreferences.setOnClickListener(btnPreferencesOnClickListener);
        btnLogout.setOnClickListener(btnLogoutOnClickListener);
        btnCheckAuthN.setOnClickListener(btnCheckAuthNOnClickListener);
        btnGetAuthN.setOnClickListener(btnGetAuthNOnClickListener);
        btnGetAuthNWithData.setOnClickListener(btnGetAuthNWithDataOnClickListener);
        btnTempPassReset.setOnClickListener(btnTempPassResetListener);
        btnCheckPreAuthZ.setOnClickListener(btnCheckPreAuthZListener);
        btnAuthorize.setOnClickListener(btnAuthorizeOnClickListener);
        btnPreauthorize.setOnClickListener(btnPreauthorizeOnClickListener);
        btnCheckAuthZ.setOnClickListener(btnCheckAuthZOnClickListener);
        btnCheckAuthZWithData.setOnClickListener(btnCheckAuthZWithDataOnClickListener);
        btnGetAuthZ.setOnClickListener(btnGetAuthZOnClickListener);
        btnGetAuthZWithData.setOnClickListener(btnGetAuthZWithDataOnClickListener);
        btnGetUserMetadata.setOnClickListener(btnGetUserMetadataClickListener);
        btnSetSelectedProvider.setOnClickListener(btnSetSelectedProviderClickListener);

        SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);

        // set default UI text edit values
        editRequestorId.setText(settings.getString(REQUESTOR_ID_PREF, "REF"));
        editResourceId.setText(settings.getString(RESOURCE_ID_PREF, "res1(^)res2"));
        editUserMetadata.setText(settings.getString(USER_METADATA_PREF, ""));

        settings = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0);

        // set the default SP URL
        SP_URL_HARDCODED = getSharedPreferences(ADOBE_PASS_PREFERENCES,0).getString(PREF_ENVIRONMENT, PRODUCTION_URL);

        // set the default signing credential
        SIGNING_CREDENTIAL_KEY_SIZE = settings.getInt(PREF_SIGNING_CREDENTIAL, 2048);

        Intent intent = new Intent(MainActivity.this, SetSoftwareStatementActivity.class);
        startActivityForResult(intent,SET_SOFTWARE_STATEMENT_ACTIVITY);

        messageDialog = new MutableLiveData<>();
        messageDialog.observe(this, new Observer<Pair<String,String>>() {
            @Override
            public void onChanged(Pair<String,String> s) {
                alertDialog(s.first, s.second);
            }
        });

        //check permissions for Android 6.0+
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously*.
                // After the user sees the explanation,
                // try again to request the permission.
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder
                        .setCancelable(false)
                        .setTitle("Adobe Pass")
                        .setMessage("Adobe Pass needs write access to you device's storage, in order to enhance your experience.\n\nPlease allow it in the next screen.")
                        .setPositiveButton("Next...", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AE_PERMISSIONS_REQUEST_CODE);
                            }
                        });
                AlertDialog alert = dialogBuilder.create();
                alert.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AE_PERMISSIONS_REQUEST_CODE);
                // AE_PERMISSIONS_REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // initializeAccessEnabler();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case AE_PERMISSIONS_REQUEST_CODE: {
                // initializeAccessEnabler();
            }
        }
    }

    private void initializeAccessEnabler() {
        // get software statement and redirecturl preferences
        String swStatement = getSharedPreferences(ADOBE_PASS_PREFERENCES,0).getString(PREF_SOFTWARE_STATEMENT,"");
        String redirectUrl = getSharedPreferences(ADOBE_PASS_PREFERENCES,0).getString(PREF_REDIRECT_URL,"");
        SP_URL_HARDCODED = getSharedPreferences(ADOBE_PASS_PREFERENCES,0).getString(PREF_ENVIRONMENT, PRODUCTION_URL);

        // create AccessEnabler
        // get a reference to the AccessEnabler instance
        // AccessEnabler can set softwareStatement and redirectUrl
        // if values for softwareStatement or redirect url are set in strings.xml, use null
        AdobePassDemoApp.setAccessEnabler(AccessEnabler.Factory.getInstance(getApplication(),SP_URL_HARDCODED,swStatement,redirectUrl));

        // configure the AccessEnabler library
        accessEnabler = AdobePassDemoApp.getAccessEnablerInstance();
        if (accessEnabler != null) {
            // set the delegate for the AccessEnabler
            accessEnabler.setDelegate(delegate);

            // update the title bar with the client version
            setTitle(getResources().getString(R.string.app_name) + " (v" + accessEnabler.getVersion() + ")");
        } else {
            trace(LOG_TAG, "Failed to configure the AccessEnabler library. ");
            // finish();
        }
    }

    private final OnClickListener btnStorageViewerOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, StorageViewerActivity.class);
            startActivity(intent);
        }
    };

    private final OnClickListener btnGlobalOptionsOnClickLister = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
            startActivityForResult(intent, APP_OPTIONS_ACTIVITY);
        }
    };

    private final OnClickListener btnSetRequestorIdOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (accessEnabler != null) {
                String requestorId = editRequestorId.getText().toString();

                // save requestorId to preferences
                SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(REQUESTOR_ID_PREF, requestorId);
                editor.commit();

                if (!"".equals(requestorId)) {
                    // request configuration data
                    ArrayList<String> spUrls = new ArrayList<String>();
                    spUrls.add(SP_URL_HARDCODED);

                    accessEnabler.setRequestor(requestorId, spUrls);

                    // show the spin-wheel
                    spWorkSpinWheel = ProgressDialog.show(MainActivity.this, "", "Talking to backend server...", true);
                } else {
                    trace(LOG_TAG, "Enter a valid requestor id.");
                }
            }
        }
    };

    private final OnClickListener btnPreferencesOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
            startActivityForResult(intent, PREFERENCES_ACTIVITY);
        }
    };

    private final OnClickListener btnLogoutOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (accessEnabler != null) {
                // do the logout
                accessEnabler.logout();
            }
        }
    };

    private final OnClickListener btnCheckAuthNOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (accessEnabler != null) accessEnabler.checkAuthentication();
        }
    };

    private final OnClickListener btnGetAuthNOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (accessEnabler != null) accessEnabler.getAuthentication();
        }
    };

    private final OnClickListener btnGetAuthNWithDataOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, GenericDataActivity.class);
            intent.putExtra("entitlement_method", "getAuthenticationWithData");
            startActivityForResult(intent, GENERIC_DATA_ACTIVITY);
        }
    };

    private final OnClickListener btnTempPassResetListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, TempPassResetActivity.class);
            String requestorId = ((EditText) findViewById(R.id.edit_requestor_id)).getText().toString();
            intent.putExtra("requestorId", requestorId);
            startActivity(intent);
        }
    };

    private final OnClickListener btnCheckPreAuthZListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (accessEnabler != null) {
                String resourceIds = editResourceId.getText().toString();

                // save resourceId to preferences
                SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(RESOURCE_ID_PREF, resourceIds);
                editor.commit();

                Switch switchCache = findViewById(R.id.switch_preauthz_cache);
                boolean cache = switchCache.isChecked();

                String[] resourcesArray = resourceIds.split("\\(\\^\\)");
                ArrayList<String> resourcesList = new ArrayList<String>(Arrays.asList(resourcesArray));
                accessEnabler.checkPreauthorizedResources(resourcesList,cache);
            }
        }
    };

    private final OnClickListener btnAuthorizeOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (accessEnabler != null) {
                String resourceId = editResourceId.getText().toString();

                // save resourceId to preferences
                SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(RESOURCE_ID_PREF, resourceId);
                editor.commit();
            }
        }
    };

    private final OnClickListener btnPreauthorizeOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (accessEnabler != null) {
                String resourceIds = editResourceId.getText().toString();

                // save resourceId to preferences
                SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(RESOURCE_ID_PREF, resourceIds);
                editor.commit();

                String[] resourcesArray = resourceIds.split("\\(\\^\\)");
                ArrayList<String> resourcesList = new ArrayList<String>(Arrays.asList(resourcesArray));

                PreauthorizeRequest request;
                Switch switchCache = findViewById(R.id.switch_preauthz_cache);
                if (switchCache != null && switchCache.isChecked()) {
                    request = new PreauthorizeRequest.Builder()
                            .setResources(resourcesList)
                            .disableFeatures()
                            .setDelegate(delegate)
                            .build();
                } else {
                    request = new PreauthorizeRequest.Builder()
                            .setResources(resourcesList)
                            .disableFeatures(PreauthorizeRequest.FEATURE.LOCAL_CACHE)
                            .setDelegate(delegate)
                            .build();
                }
                accessEnabler.preauthorize(request, new AccessEnablerCallback<PreauthorizeResponse>() {
                    @Override
                    public void onResponse(PreauthorizeResponse result) {
                        Log.d(LOG_TAG, "Success");

                        String message = "";
                        if (result != null) {
                            if (result.getStatus() == null) {
                                ArrayList<Decision> decisions = result.getResources();
                                for (Decision decision : decisions) {
                                    if ((decision.getError() == null) && (decision.isAuthorized())) {
                                        message += decision.getId() + "\n";
                                    }
                                }
                            } else {
                            message = "None";
                            }
                        }

                        Pair<String,String> alertMessage = new Pair<>("Preauthorized resources", message);
                        messageDialog.postValue(alertMessage);
                    }

                    @Override
                    public void onFailure(PreauthorizeResponse result) {
                        Log.d(LOG_TAG, "Failure");

                        String message = "None";
                        Pair<String,String> alertMessage = new Pair<>("Preauthorized resources", message);
                        messageDialog.postValue(alertMessage);
                    }
                });
            }
        }
    };

    private final OnClickListener btnCheckAuthZOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (accessEnabler != null) {
                String resourceId = editResourceId.getText().toString();

                // save resourceId to preferences
                SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(RESOURCE_ID_PREF, resourceId);
                editor.commit();

                accessEnabler.checkAuthorization(resourceId);
            }
        }
    };

    private final OnClickListener btnCheckAuthZWithDataOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            String resourceId = editResourceId.getText().toString();

            // save resourceId to preferences
            SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(RESOURCE_ID_PREF, resourceId);
            editor.commit();

            // display generic data screen
            Intent intent = new Intent(MainActivity.this, GenericDataActivity.class);
            intent.putExtra("resource_id", resourceId);
            intent.putExtra("entitlement_method", "checkAuthorizationWithData");
            startActivityForResult(intent, GENERIC_DATA_ACTIVITY);
        }
    };

    private final OnClickListener btnGetAuthZOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (accessEnabler != null) {
                String resourceId = editResourceId.getText().toString();

                // save resourceId to preferences
                SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(RESOURCE_ID_PREF, resourceId);
                editor.commit();

                accessEnabler.getAuthorization(resourceId);
            }
        }
    };

    private final OnClickListener btnGetAuthZWithDataOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            String resourceId = editResourceId.getText().toString();

            // save resourceId to preferences
            SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(RESOURCE_ID_PREF, resourceId);
            editor.commit();

            // display generic data screen
            Intent intent = new Intent(MainActivity.this, GenericDataActivity.class);
            intent.putExtra("resource_id", resourceId);
            intent.putExtra("entitlement_method", "getAuthorizationWithData");
            startActivityForResult(intent, GENERIC_DATA_ACTIVITY);
        }
    };

    private final OnClickListener btnGetUserMetadataClickListener = new OnClickListener() {
        public void onClick(View view) {
            if (accessEnabler != null) {
                String metadataName = editUserMetadata.getText().toString();

                if (metadataName.length() > 0) {
                    MetadataKey key;

                    if ("AUTHN_TTL".equals(metadataName)) {
                        key = new MetadataKey(UserProfileService.METADATA_KEY_TTL_AUTHN);
                    } else if ("AUTHZ_TTL".equals(metadataName)) {
                        key = new MetadataKey(UserProfileService.METADATA_KEY_TTL_AUTHZ);
                        key.addArgument(new Pair(
                                UserProfileService.METADATA_ARG_RESOURCE_ID, editResourceId.getText().toString()));
                    } else if ("DEVICEID".equals(metadataName)) {
                        key = new MetadataKey(UserProfileService.METADATA_KEY_DEVICE_ID);
                    } else {
                        // save user metadata to preferences
                        SharedPreferences settings = getSharedPreferences(MAIN_ACTIVITY_UI_PREFERENCES, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(USER_METADATA_PREF, metadataName);
                        editor.commit();

                        key = new MetadataKey(UserProfileService.METADATA_KEY_USER_META);
                        key.addArgument(new Pair(UserProfileService.METADATA_ARG_USER_META, metadataName));
                    }

                    accessEnabler.getMetadata(key);
                } else {
                    trace(LOG_TAG, "Enter a valid metadata id.");
                }
            }
        }
    };

    private final OnClickListener btnSetSelectedProviderClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (accessEnabler != null) {
                String selectedProviderId = editSelectedProvider.getText().toString();

                if (selectedProviderId.length() > 0) {
                    if (selectedProviderId.equals("null")) {
                        accessEnabler.setSelectedProvider(null);
                    } else
                        accessEnabler.setSelectedProvider(selectedProviderId);
                }
            }
        }
    };

    public interface MessageHandler {
        void handle(Bundle bundle);
    }

    private WebView createLogoutWebView() {
        // setup the logout WebView (hidden)
        WebView webView = new WebView(MainActivity.this);
        // enable JavaScript support
        WebSettings browserSettings = webView.getSettings();
        browserSettings.setJavaScriptEnabled(true);
        browserSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // install listeners for various page load events
        webView.setWebViewClient(webViewClient);

        return webView;
    }

    private final WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(LOG_TAG, description);
            Log.d(LOG_TAG, failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(LOG_TAG, "Page started: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(LOG_TAG, "Page loaded: " + url);

            // if we detect a redirect to our application URL, this is an indication
            // that the logout workflow was completed successfully
            if (url.startsWith(AccessEnablerConstants.ADOBEPASS_REDIRECT_URL_SCHEME)) {
                // dismiss the spin-wheel
                spWorkSpinWheel.dismiss();

                alertDialog("Logout", "SUCCESS") ;
            }

            super.onPageFinished(view, url);
        }
    };

    private MessageHandler[] messageHandlers = new MessageHandler[] {
            new MessageHandler() { public void handle(Bundle bundle) { handleSetRequestor(bundle); } },             //  0 SET_REQUESTOR_COMPLETE
            new MessageHandler() { public void handle(Bundle bundle) { handleSetAuthnStatus(bundle); } },           //  1 SET_AUTHN_STATUS
            new MessageHandler() { public void handle(Bundle bundle) { handleSetToken(bundle); } },                 //  2 SET_TOKEN
            new MessageHandler() { public void handle(Bundle bundle) { handleSetTokenRequestFailed(bundle); } },    //  3 TOKEN_REQUEST_FAILED
            new MessageHandler() { public void handle(Bundle bundle) { handleSelectedProvider(bundle); } },         //  4 SELECTED_PROVIDER
            new MessageHandler() { public void handle(Bundle bundle) { handleDisplayProviderDialog(bundle); } },    //  5 DISPLAY_PROVIDER_DIALOG
            new MessageHandler() { public void handle(Bundle bundle) { handleNavigateToUrl(bundle); } },            //  6 NAVIGATE_TO_URL
            new MessageHandler() { public void handle(Bundle bundle) { handleSendTrackingData(bundle); } },         //  7 SEND_TRACKING_DATA
            new MessageHandler() { public void handle(Bundle bundle) { handleSetMetadataStatus(bundle); } },        //  8 SET_METADATA_STATUS
            new MessageHandler() { public void handle(Bundle bundle) { handlePreauthorizedResources(bundle); } },   //  9 PREAUTHORIZED_RESOURCES
            new MessageHandler() { public void handle(Bundle bundle) { handleAdvancedStatus(bundle); } },           //  10 ADVANCED_STATUS
    };

    private void handleMessage(Bundle bundle) {
        String message = bundle.getString("message");
        Log.d(LOG_TAG, message);
    }

    private void handleSetRequestor(Bundle bundle) {
        // extract the status of the setRequestor() API call
        int status = bundle.getInt("status");

        switch (status) {
            case (AccessEnablerConstants.ACCESS_ENABLER_STATUS_SUCCESS): {
                // set requestor operation was successful - enable the authN/Z controls
                authnViewGroup.setVisibility(View.VISIBLE);
                authzViewGroup.setVisibility(View.VISIBLE);
                userMetadataViewGroup.setVisibility(View.VISIBLE);

                alertDialog("Config phase", "SUCCESS");
            } break;
            case (AccessEnablerConstants.ACCESS_ENABLER_STATUS_ERROR): {
                // set requestor operation failed - disable the authN/Z controls
                authnViewGroup.setVisibility(View.GONE);
                authzViewGroup.setVisibility(View.GONE);
                userMetadataViewGroup.setVisibility(View.GONE);

                alertDialog("Config phase", "FAILED");
            } break;
            default: {
                throw new RuntimeException("setRequestor(): Unknown status code.");
            }
        }

        // dismiss the progress dialog
        spWorkSpinWheel.dismiss();
    }

    private void handleSetAuthnStatus(Bundle bundle) {
        // extract the status code
        int status = bundle.getInt("status");
        String errCode = bundle.getString("err_code");

        switch (status) {
            case (AccessEnablerConstants.ACCESS_ENABLER_STATUS_SUCCESS): {
                alertDialog("Authentication", "SUCCESS");
            } break;
            case (AccessEnablerConstants.ACCESS_ENABLER_STATUS_ERROR): {
                alertDialog("Authentication", "FAILED\n" + errCode);
            } break;
            default: {
                throw new RuntimeException("setAuthnStatus(): Unknown status code.");
            }
        }
    }

    private void handleSetToken(Bundle bundle) {
        // extract the token and resource ID
        String resourceId = bundle.getString("resource_id");
        String token = bundle.getString("token");

        String error = null;
        if (token == null || token.trim().length() == 0) {
            error = "empty token";
        }

        if (error == null)
            alertDialog("Authorization", "SUCCESS\n\nValidated media token\n\nResource: " + resourceId);
        else
            alertDialog("Authorization", "FAILED\n\nFailed media token validation\n\nResource: " + resourceId + "\nError: " + error);

        Log.d (LOG_TAG, resourceId);
        Log.d(LOG_TAG, "Token: " + token);
    }

    private void handleSetTokenRequestFailed(Bundle bundle) {
        // extract the error details and resource ID
        String resourceId = bundle.getString("resource_id");
        String errorCode = bundle.getString("err_code");
        String errorDescription = bundle.getString("err_description");

        alertDialog("Authorization", "FAILED\n\nFor resource: " + resourceId +
                "\n\nERROR: " + errorCode +
                "\n\nERROR DETAILS: " + errorDescription);
    }

    private void handleSelectedProvider(Bundle bundle) {
        // extract the MVPD ID
        String mvpdId = bundle.getString("mvpd_id");

        alertDialog("Selected MVPD", (mvpdId == null) ? "None" : mvpdId);
    }

    private void handleDisplayProviderDialog(Bundle bundle) {
        handleMessage(bundle);

        // start the activity that handles the MVPD selection process
        Intent intent = new Intent(MainActivity.this, MvpdPickerActivity.class);
        intent.putExtra("mvpd_bundled_data", bundle);
        startActivityForResult(intent, MVPD_PICKER_ACTIVITY);
    }

    private void handleNavigateToUrl(Bundle bundle) {
        handleMessage(bundle);

        String targetUrl = bundle.getString("url");

        if (targetUrl.indexOf(AccessEnablerConstants.SP_URL_PATH_GET_AUTHENTICATION) > 0) {
            // start the activity that handles the MVPD login process
            Intent intent = new Intent(MainActivity.this, MvpdLoginActivity.class);
            intent.putExtra("url", bundle.getString("url"));
            startActivityForResult(intent, MVPD_LOGIN_ACTIVITY);
        } else if (targetUrl.indexOf(AccessEnablerConstants.SP_URL_PATH_LOGOUT) > 0) {
            // show the spin-wheel
            spWorkSpinWheel = ProgressDialog.show(MainActivity.this, "", "Talking to backend server...", true);

            // instantiate the logout WebView
            logoutWebView = createLogoutWebView();

            // go to the logout URL
            logoutWebView.loadUrl(targetUrl);
        }
    }

    private void handleSendTrackingData(Bundle bundle) {
        // extract the event type and the event data
        int eventType = bundle.getInt("event_type");
        ArrayList<String> data = bundle.getStringArrayList("event_data");

        String message = "";
        String eventName;
        int index = 0;

        switch (eventType) {
            case (Event.EVENT_MVPD_SELECTION): {
                eventName = "mvpd selection";

                message += "MVPD ID: " + data.get(index) + "\n\n"; index ++;
            } break;

            case (Event.EVENT_AUTHN_DETECTION): {
                eventName = "authentication detection";

                message += "SUCCESSFUL: " + (data.get(index).equals("true") ? "YES" : "NO") + "\n\n"; index ++;
                message += "MVPD ID: " + data.get(index) + "\n\n"; index ++;
                message += "GUID: " + data.get(index) + "\n\n"; index ++;
                message += "CACHED: " + data.get(index) + "\n\n"; index ++;
                message += "REGCODE: " + data.get(index) + "\n\n"; index ++;

            } break;

            case (Event.EVENT_AUTHZ_DETECTION): {
                eventName = "authorization detection";
                message += "SUCCESSFUL: " + (data.get(index).equals("true") ? "YES" : "NO") + "\n\n"; index ++;
                message += "MVPD ID: " + data.get(index) + "\n\n"; index ++;
                message += "GUID: " + data.get(index) + "\n\n"; index ++;
                message += "CACHED: " + data.get(index) + "\n\n"; index ++;
                message += "ERROR: " + data.get(index) + "\n\n"; index ++;
                message += "ERROR DETAILS: " + data.get(index) + "\n\n"; index ++;
            } break;

            case (Event.EVENT_LOGOUT): {
                eventName = "logout detection";
                message += "SUCCESSFUL: " + (data.get(index).equals("true") ? "YES" : "NO") + "\n\n"; index++;
            } break;

            default: {
                throw new RuntimeException("setTrackingData(): Unknown event type.");
            }
        }

        message += "DEVICE TYPE: " + data.get(index) + "\n\n"; index ++;
        message += "CLIENT TYPE: " + data.get(index) + "\n\n"; index ++;
        message += "OS: " + data.get(index) + "\n\n";

        alertDialog("Tracking event", "EVENT: " + eventName + "\n\n" + message);
    }

    private void handleSetMetadataStatus(Bundle bundle) {
        // extract the key and the result
        MetadataKey key = (MetadataKey) bundle.getSerializable("key");
        MetadataStatus result = (MetadataStatus) bundle.getSerializable("result");

        switch (key.getKey()) {
            case UserProfileService.METADATA_KEY_TTL_AUTHN: {
                String ttl = "None";
                if (result != null && result.getSimpleResult() != null) {
                    ttl = result.getSimpleResult();
                }
                alertDialog("AuthN token TTL", ttl);
            } break;

            case UserProfileService.METADATA_KEY_TTL_AUTHZ: {
                String resourceId = key.getArgument(UserProfileService.METADATA_ARG_RESOURCE_ID);
                String ttl = "None";
                if (result != null && result.getSimpleResult() != null) {
                    ttl = result.getSimpleResult();
                }
                alertDialog("AuthZ token TTL", "For resource: " + resourceId + "\n\n" + ttl);
            } break;

            case UserProfileService.METADATA_KEY_DEVICE_ID: {
                String deviceId = "None";
                if (result != null && result.getSimpleResult() != null) {
                    deviceId = result.getSimpleResult();
                }
                alertDialog("Device ID", "".equals(deviceId) ? "None" : deviceId);
            } break;

            case UserProfileService.METADATA_KEY_USER_META: {
                String metadataName = key.getArgument(UserProfileService.METADATA_ARG_USER_META);
                Object metadataValue = null;
                boolean isEncrypted = false;

                if (result != null && result.getUserMetadataResult() != null) {
                    isEncrypted = result.isEncrypted();
                    metadataValue = result.getUserMetadataResult();

                }

                String message = "Key: " + (metadataName != null ? metadataName : "None") +
                        "\nEncrypted: " + (isEncrypted ? "YES" : "NO") +
                        "\nValue: " + (metadataValue != null ? metadataValue : "None");

                Log.d(LOG_TAG, "getUserMetadata:\n" + message);
                ((EditText) findViewById(R.id.edit_user_metadata_status)).setText(message);

                alertDialog("User Metadata", message);
            }
            break;

            default: {
                throw new RuntimeException("setRequestor(): Unknown status code.");
            }
        }
    }

    private void handlePreauthorizedResources(Bundle bundle) {
        // extract the pre-authz resource list
        ArrayList<String> resources = bundle.getStringArrayList("resources");

        String message = (resources.size() == 0) ? "None" : Utils.joinStrings(resources, "\n");
        alertDialog("Preauthorized resources", message);
    }

    private void handleAdvancedStatus(Bundle bundle) {
        String id = bundle.getString("id");
        String level = bundle.getString("level");
        String message = bundle.getString("message");
        String resource = bundle.getString("resource");

        String alertMessage = "errorId: " + id + "\n" + "level: " + level + "\n" + "message: " + message;
        if (resource != null) {
            alertMessage += "\n resource: " + resource;
        }

        alertDialog("Status", alertMessage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case (SET_SOFTWARE_STATEMENT_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        initializeAccessEnabler();
                        break;
                    }
                    default: {
                        trace(LOG_TAG, "Invalid software statement");
                    }
                }
            } break;
            case (MVPD_PICKER_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        Mvpd mvpd = (Mvpd) data.getSerializableExtra("mvpd");
                        trace(LOG_TAG, "Selected: " + mvpd.getDisplayName());

                        // user has selected an MVPD: call setSelectedProvider()
                        accessEnabler.setSelectedProvider(mvpd.getId());
                    } break;
                    case (RESULT_CANCELED): {
                        trace(LOG_TAG, "Selection canceled.");

                        // abort the authN flow.
                        accessEnabler.setSelectedProvider(null);
                    } break;
                    default: {
                        trace(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            case (APP_OPTIONS_ACTIVITY) : {
                switch (resultCode) {
                    case (RESULT_OK): {
                        HashMap<String, String> options = new HashMap();
                        String app_profile = data.getStringExtra("applicationProfile");
                        options.put("applicationProfile",app_profile);
                        String visitor_id = data.getStringExtra("ap_vi");
                        String ad_id = data.getStringExtra("ap_ai");
                        options.put("ap_vi",visitor_id);
                        options.put("ap_ai",ad_id);
                        String device_info = data.getStringExtra("device_info");
                        options.put("device_info",device_info);
                        accessEnabler.setOptions(options);
                    } break;
                    default: {
                        trace(LOG_TAG, "Application options not set.");
                    }
                }
            } break;
            case (MVPD_LOGIN_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        // retrieve the authentication token
                        accessEnabler.getAuthenticationToken();
                    } break;
                    case (RESULT_CANCELED): {
                        trace(LOG_TAG, "Login canceled.");

                        // abort the authN flow.
                        accessEnabler.setSelectedProvider(null);
                    } break;
                    default: {
                        trace(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            case (MVPD_LOGOUT_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        trace(LOG_TAG, "Logout successful.");
                    } break;
                    case (RESULT_CANCELED): {
                        trace(LOG_TAG, "Logout canceled.");
                    } break;
                    default: {
                        trace(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            case (PREFERENCES_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        SP_URL_HARDCODED = (String) data.getSerializableExtra("url");
                        SIGNING_CREDENTIAL_KEY_SIZE = (Integer) data.getSerializableExtra("signing_credential");
                    } break;
                    case (RESULT_CANCELED): {
                        Log.d(LOG_TAG, "Preferences activity canceled.");
                    } break;
                    default: {
                        Log.d(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            case (GENERIC_DATA_ACTIVITY): {
                switch (resultCode) {
                    case (RESULT_OK): {
                        if (accessEnabler != null) {
                            String entitlementMethod = (String) data.getSerializableExtra("entitlement_method");
                            String genericData = (String) data.getSerializableExtra("generic_data");

                            Map<String, Object> ugenericDataMap;
                            try {
                                Gson gson = new Gson();
                                ugenericDataMap = gson.fromJson(genericData, Map.class);
                                Log.d(LOG_TAG, "Generic Data: " + genericData);
                            } catch (Exception e) {
                                Log.d(LOG_TAG, e.toString());
                                Log.d(LOG_TAG, "Error deserializing generic data string.");
                                return;
                            }

                            if (entitlementMethod.equals("getAuthenticationWithData")) {
                                Boolean forceAuthn = Boolean.valueOf((String) data.getSerializableExtra("force_authn"));
                                Log.d(LOG_TAG, "Force Authn: " + forceAuthn);

                                accessEnabler.getAuthentication(forceAuthn, ugenericDataMap);
                            } else if (entitlementMethod.equals("checkAuthorizationWithData")) {
                                String resourceId = (String) data.getSerializableExtra("resource_id");
                                Log.d(LOG_TAG, "Resource ID: " + resourceId);

                                accessEnabler.checkAuthorization(resourceId, ugenericDataMap);
                            } else if (entitlementMethod.equals("getAuthorizationWithData")) {
                                String resourceId = (String) data.getSerializableExtra("resource_id");
                                Log.d(LOG_TAG, "Resource ID: " + resourceId);

                                accessEnabler.getAuthorization(resourceId, ugenericDataMap);
                            } else {
                                Log.d(LOG_TAG, "Unknown entitlement method: " + entitlementMethod);
                            }
                        }
                    } break;
                    case (RESULT_CANCELED): {
                        Log.d(LOG_TAG, "Generic Data activity canceled.");
                    } break;
                    default: {
                        Log.d(LOG_TAG, "Cannot handle activity result.");
                    }
                }
            } break;
            default: {
                trace(LOG_TAG, "Unknown activity.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu resource
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.application_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MetadataKey key;

        if (accessEnabler != null) {
            // Handle item selection
            switch (item.getItemId()) {
                case (R.id.authn_exp): {
                    key = new MetadataKey(UserProfileService.METADATA_KEY_TTL_AUTHN);
                }
                break;
                case (R.id.authz_exp): {
                    key = new MetadataKey(UserProfileService.METADATA_KEY_TTL_AUTHZ);
                    key.addArgument(new Pair(
                            UserProfileService.METADATA_ARG_RESOURCE_ID, editResourceId.getText().toString()));
                }
                break;
                case (R.id.device_id): {
                    key = new MetadataKey(UserProfileService.METADATA_KEY_DEVICE_ID);
                }
                break;
                case (R.id.current_mvpd): {
                    accessEnabler.getSelectedProvider();
                    return true;
                }
                default: {
                    return super.onOptionsItemSelected(item);
                }
            }

            accessEnabler.getMetadata(key);
        }
        return true;
    }
}
