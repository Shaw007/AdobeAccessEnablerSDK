/*
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2020 Adobe Systems Incorporated
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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.adobe.adobepass.accessenabler.utils.Utils;
import com.adobe.adobepass.apps.demo.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TempPassResetActivity extends AbstractActivity {

    private static final String LOG_TAG = "TempPassResetActivity";

    private static final String TPR_PREFERENCES = "tprPreferences";
    private static final String PREF_TPR_ENV = "tprEnv";
    private static final String PREF_TPR_CUSTOM_URL = "tprCustomUrl";
    private static final String PREF_TPR_OAUTH2_BEARER_TOKEN = "tprOAuth2BearerToken";
    private static final String PREF_TPR_MVPD_ID = "tprMvpdId";
    private static final String PREF_TPR_GENERIC_KEY = "tprGenericKey";

    private static final String RELEASE_PRODUCTION_URL = "mgmt.auth.adobe.com";
    private static final String RELEASE_STAGING_URL = "mgmt.auth-staging.adobe.com";
    private static final String PREQUAL_PRODUCTION_URL = "mgmt-prequal.auth.adobe.com";
    private static final String PREQUAL_STAGING_URL = "mgmt-prequal.auth-staging.adobe.com";

    private static String RESET_DEVICE_ID_SERVICE_v2 = "/reset-tempass/v2/reset";
    private static String RESET_DEVICE_ID_SERVICE_v2_1 = "/reset-tempass/v2.1/reset";

    private Map<Integer, String> environmentsUrl;

    private RadioGroup radioGroup;
    private EditText editCustomUrl;
    private String requestorId;
    private EditText oauth2BearerTokenEdit;
    private EditText mvpdIdEdit;
    private EditText genericKeyEdit;

    private Integer noNetworkingThreads = 0;
    private ProgressDialog progressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.temp_pass_reset);

        radioGroup = (RadioGroup) findViewById(R.id.tpr_radio_group_environment_url);
        editCustomUrl = (EditText) findViewById(R.id.tpr_edit_custom_url);
        oauth2BearerTokenEdit = (EditText) findViewById(R.id.tpr_edit_oauth2_bearer_token);
        mvpdIdEdit = (EditText) findViewById(R.id.tpr_edit_temp_pass_id);
        genericKeyEdit = (EditText) findViewById(R.id.tpr_edit_generic_key);

        Button btnReset = (Button) findViewById(R.id.tpr_btn_reset);
        Button btnCancel = (Button) findViewById(R.id.tpr_btn_back_to_main);

        radioGroup.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener);
        btnReset.setOnClickListener(btnResetListener);
        btnCancel.setOnClickListener(btnCancelListener);

        requestorId = getIntent().getStringExtra("requestorId");

        // populate UI from preferences
        SharedPreferences settings = getSharedPreferences(TPR_PREFERENCES, 0);
        oauth2BearerTokenEdit.setText(settings.getString(PREF_TPR_OAUTH2_BEARER_TOKEN, ""));
        mvpdIdEdit.setText(settings.getString(PREF_TPR_MVPD_ID, ""));
        genericKeyEdit.setText(settings.getString(PREF_TPR_GENERIC_KEY, ""));

        initializeEvironmentInfo();
        selectEnvironment(settings.getInt(PREF_TPR_ENV, R.id.radio_tpr_release_staging), settings.getString(PREF_TPR_CUSTOM_URL, ""));
    }

    private void initializeEvironmentInfo() {
        environmentsUrl = new HashMap<>();
        environmentsUrl.put(R.id.radio_tpr_release_production,  RELEASE_PRODUCTION_URL);
        environmentsUrl.put(R.id.radio_tpr_release_staging,     RELEASE_STAGING_URL);
        environmentsUrl.put(R.id.radio_tpr_prequal_production,  PREQUAL_PRODUCTION_URL);
        environmentsUrl.put(R.id.radio_tpr_prequal_staging,     PREQUAL_STAGING_URL);
    }

    private void selectEnvironment(int desiredEnvironment, String desiredCustomUrl) {
        if (environmentsUrl.containsKey(desiredEnvironment)) {
            radioGroup.check(desiredEnvironment);
            editCustomUrl.setEnabled(false);
            editCustomUrl.setText("");
        } else {
            radioGroup.check(R.id.radio_tpr_custom);
            editCustomUrl.setEnabled(true);
            editCustomUrl.setText(desiredCustomUrl);
        }
    }

    private final RadioGroup.OnCheckedChangeListener radioGroupOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            int desiredEnvironmentId = radioGroup.getCheckedRadioButtonId();

            editCustomUrl.setEnabled(!environmentsUrl.containsKey(desiredEnvironmentId));

            if (!editCustomUrl.isEnabled()) {
                editCustomUrl.setText("");
            }
        }
    };

    private final View.OnClickListener btnResetListener = new View.OnClickListener() {
        public void onClick(View view) {
            String token = oauth2BearerTokenEdit.getText().toString().trim();
            String mvpdId = mvpdIdEdit.getText().toString().trim();
            String genericKey = genericKeyEdit.getText().toString().trim();

            String deviceId = getDeviceId();
            String environmentUrl;
            String oauth2BearerToken;

            if (token.equals("") || mvpdId.equals("")) {
                alertDialog("Temp Pass Reset", "Please provide the OAuth2 Bearer Token and MVPD ID");
                return;
            }

            int desiredEnvironmentId = radioGroup.getCheckedRadioButtonId();

            if (environmentsUrl.containsKey(desiredEnvironmentId)) {
                environmentUrl = environmentsUrl.get(desiredEnvironmentId) + RESET_DEVICE_ID_SERVICE_v2_1;
                oauth2BearerToken = token;
            } else {
                environmentUrl = editCustomUrl.getText().toString().trim() + RESET_DEVICE_ID_SERVICE_v2;
                oauth2BearerToken = getOAuth2BearerToken(environmentUrl, token);
            }

            if (oauth2BearerToken != null) {
                progressDialog = ProgressDialog.show(TempPassResetActivity.this, "", "Talking to backend server...", true);

                Log.d(LOG_TAG, "Resetting Temp Pass for Device ID: " + deviceId);

                String resetUrl = buildResetTPUrlForDeviceId(environmentUrl, deviceId, requestorId, mvpdId);
                synchronized (this) {
                    noNetworkingThreads++;
                }
                new TempPassResetRunner("Device ID").execute(resetUrl, oauth2BearerToken);

                if (!genericKey.trim().equals("")) {
                    Log.d(LOG_TAG, "Resetting Temp Pass for Generic Key: " + genericKey);

                    resetUrl = buildResetTPUrlForGenericKey(environmentUrl + "/generic", genericKey, requestorId, mvpdId);
                    synchronized (this) {
                        noNetworkingThreads++;
                    }
                    new TempPassResetRunner("Generic Key").execute(resetUrl, oauth2BearerToken);
                }
            } else {
                alertDialog("Temp Pass Reset", "FAILED");
            }

            // save UI values to preferences
            SharedPreferences settings = getSharedPreferences(TPR_PREFERENCES, 0);
            SharedPreferences.Editor editor = settings.edit();

            editor.putString(PREF_TPR_OAUTH2_BEARER_TOKEN, token);
            editor.putString(PREF_TPR_MVPD_ID, mvpdId);
            editor.putString(PREF_TPR_GENERIC_KEY, genericKey);
            editor.putInt(PREF_TPR_ENV, desiredEnvironmentId);
            if (!environmentsUrl.containsKey(desiredEnvironmentId)) {
                editor.putString(PREF_TPR_CUSTOM_URL, environmentUrl);
            }

            editor.apply();
        }
    };

    private String buildResetTPUrlForDeviceId(String environmentUrl, String deviceId, String requestorId, String mvpdId) {
        return new StringBuilder()
                .append("https://")
                .append(environmentUrl)
                .append("?device_id=").append(deviceId)
                .append("&requestor_id=").append(requestorId)
                .append("&mvpd_id=").append(mvpdId)
                .toString();
    }

    private String buildResetTPUrlForGenericKey(String environmentUrl, String genericKey, String requestorId, String mvpdId) {
        return new StringBuilder()
                .append("https://")
                .append(environmentUrl)
                .append("?key=").append(genericKey)
                .append("&requestor_id=").append(requestorId)
                .append("&mvpd_id=").append(mvpdId)
                .toString();
    }

    private final View.OnClickListener btnCancelListener = new View.OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(TempPassResetActivity.this, MainActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    };

    private static class Task extends AsyncTask<HttpPost, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(HttpPost... httpPosts) {
            try {
                HttpResponse response = new DefaultHttpClient().execute(httpPosts[0], new BasicHttpContext());
                return new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private String getOAuth2BearerToken(String environmentUrl, String softwareStatement) {
        try {
            if (!"sp.auth.adobe.com".equals(environmentUrl) && !"sp.auth-staging.adobe.com".equals(environmentUrl)) {
                environmentUrl = "sp.auth.adobe.com";
            }

            HttpPost applicationRegistrationRequest = new HttpPost("https://" + environmentUrl + "/o/client/register");

            String applicationRegistrationBodyString = "{\"software_statement\":" + "\"" + softwareStatement + "\"}";
            StringEntity applicationRegistrationBodyEntity = new StringEntity(applicationRegistrationBodyString);
            applicationRegistrationRequest.setEntity(applicationRegistrationBodyEntity);

            applicationRegistrationRequest.setHeader("Content-Type", "application/json");
            applicationRegistrationRequest.setHeader("Accept", "application/json");

            JSONObject applicationRegistration = new Task().execute(applicationRegistrationRequest).get();

            String accessTokenUrl = "https://" + environmentUrl + "/o/client/token?" + "client_id=" + applicationRegistration.getString("client_id") +
                    "&client_secret=" + applicationRegistration.getString("client_secret") + "&grant_type=client_credentials";

            HttpPost accessTokenRequest = new HttpPost(accessTokenUrl);

            accessTokenRequest.setHeader("Content-Type", "application/json");
            accessTokenRequest.setHeader("Accept", "application/json");

            JSONObject accessToken = new Task().execute(accessTokenRequest).get();

            return accessToken.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    private class TempPassResetRunner extends AsyncTask<String, Void, Integer> {
        private String label;

        public TempPassResetRunner(String label) {
            this.label = label;
        }

        @Override
        protected Integer doInBackground(String... params) {
            String targetUrl = params[0];
            String oauth2BearerToken = params[1];

            Log.d(LOG_TAG, "Target URL: " + targetUrl);
            return performHttpDelete(targetUrl, oauth2BearerToken);
        }

        private Integer performHttpDelete(String targetUrl, String oauth2BearerToken) {
            try {
                HttpDelete httpDelete = new HttpDelete(targetUrl);
                httpDelete.setHeader("Authorization", "Bearer " + oauth2BearerToken);
                HttpResponse httpResponse = new DefaultHttpClient().execute(httpDelete, new BasicHttpContext());
                return httpResponse.getStatusLine().getStatusCode();
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(LOG_TAG, "Temp Pass Reset for " + label + " finished with status: " + result);
            alertDialog("Temp Pass Reset", result == 204 ? "SUCCESS" : "FAILED");

            synchronized (TempPassResetActivity.this) {
                if (--noNetworkingThreads == 0) {
                    progressDialog.dismiss();
                }
            }
        }
    }

    private String getDeviceId() {
        return Utils.hash(Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID),"sha-256");
    }
}