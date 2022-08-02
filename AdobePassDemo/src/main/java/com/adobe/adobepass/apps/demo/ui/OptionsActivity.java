package com.adobe.adobepass.apps.demo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.adobe.adobepass.apps.demo.R;
import com.adobe.adobepass.apps.demo.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by babeanu on 5/25/17.
 */

public class OptionsActivity extends AbstractActivity {

    public static final String ADOBE_PASS_APPINFO = "adobePassAppInfo";
    public static final String APPLICATION_PROFILE = "applicationProfile";
    public static final String VISITOR_ID = "ap_vi";
    public static final String AD_ID = "ap_ai";
    public static final String DEVICE_INFO = "device_info";

    /** The type of the de device and the value may be "Tablet", "Desktop", "MobilePhone", "SetTopBox", "GamesConsole", "TV" or "MediaPlayer" **/
    public static final String PRIMARY_HARDWARE_TYPE ="primaryHardwareType";

    /** The model name of a device, browser or some other component  **/
    public static final String MODEL ="model";

    /** The version of the device **/
    public static final String VERSION = "version" ;

    /** The company that provides a device **/
    public static final String VENDOR = "vendor" ;

    /** Primary organization creating the device. **/
    public static final String MANUFACTURER = "manufacturer" ;

    /** The name of the Operating System installed on the device. **/
    public static final String OS_NAME = "osName" ;

    /** The general group name of the operating system. **/
    public static final String OS_FAMILY = "osFamily" ;

    /** The supplier of the operating system. **/
    public static final String OS_VENDOR = "osVendor" ;

    /** The version of the operating system. **/
    public static final String OS_VERSION = "osVersion" ;

    /** The name or type of the browser on the device. **/
    public static final String BROWSER_NAME = "browserName" ;

    /** The browser version on the device. **/
    public static final String BROWSER_VERSION = "browserVersion";

    /** The supplier of the web browser. **/
    public static final String BROWSER_VENDOR = "browserVendor" ;

    /** Browser's user agent **/
    public static final String USER_AGENT = "userAgent" ;

    /** The display width of the device **/
    public static final String DISPLAY_WIDTH = "displayWidth" ;

    /** The display height of the device **/
    public static final String DISPLAY_HEIGHT = "displayHeight" ;

    /** Display pixel density of the device **/
    public static final String DISPLAY_PPI = "displayPpi" ;

    /** The physical size of a device or some other component. **/
    public static final String DIAGONAL_SCREEN_SIZE = "diagonalScreenSize" ;

    /** The type of the connection (WIFI HTTP/3 for example) **/
    public static final String CONNECTION_TYPE = "connectionType" ;

    /** Flag indicating the network is secure or not. **/
    public static final String CONNECTION_SECURE = "connectionSecure" ;

    /** The source port of network connection **/
    public static final String CONNECTION_PORT = "connectionPort" ;

    /** The unique identifier of the application **/
    public static final String APPLICATION_ID = "applicationId" ;

    enum DEVICE_INFO_ATTRIBUTES { PRIMARY_HARDWARE_TYPE, MODEL, VENDOR, MANUFACTURER, OS_NAME};

    private EditText mEditAppProfile;
    private EditText mEditVisitorId;
    private EditText mEditAdId;
    private EditText mEditprimaryHardwareType;
    private EditText mEditmodel;
    private EditText mEditversion;
    private EditText mEditvendor;
    private EditText mEditmanufacturer;
    private EditText mEditosName;
    private EditText mEditosFamily;
    private EditText mEditosVendor;
    private EditText mEditosVersion;
    private EditText mEditbrowserName;
    private EditText mEditbrowserVersion;
    private EditText mEditbrowserVendor;
    private EditText mEdituserAgent;
    private EditText mEditdisplayWidth;
    private EditText mEditdisplayHeight;
    private EditText mEditdisplayPpi;
    private EditText mEditdiagonalScreenSize;
    private EditText mEditconnectionType;
    private EditText mEditconnectionSecure;
    private EditText mEditconnectionPort;
    private EditText mEditapplicationId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.global_options);

        Button btnOk = ( Button) findViewById(R.id.btn_set_option);
        Button btnBack = (Button) findViewById(R.id.btn_back);

        btnOk.setOnClickListener(btnOkOnClickListener);
        btnBack.setOnClickListener(btnCancelOnClickListener);

        mEditAppProfile = (EditText)findViewById(R.id.editText_application_profile);
        mEditVisitorId = (EditText) findViewById(R.id.editText_visitor_id);
        mEditAdId = (EditText) findViewById(R.id.editText_ad_id);
        mEditprimaryHardwareType = (EditText) findViewById(R.id.editText_primary_hardware_type);
        mEditmodel = (EditText) findViewById(R.id.editText_model);
        mEditversion = (EditText) findViewById(R.id.editText_version);
        mEditvendor = (EditText) findViewById(R.id.editText_vendor);
        mEditmanufacturer = (EditText) findViewById(R.id.editText_manufacturer);
        mEditosName = (EditText) findViewById(R.id.editText_osName);
        mEditosFamily = (EditText) findViewById(R.id.editText_osFamily);
        mEditosVendor = (EditText) findViewById(R.id.editText_vendor);
        mEditosVersion = (EditText) findViewById(R.id.editText_osVersion);
        mEditbrowserName = (EditText) findViewById(R.id.editText_browserName);
        mEditbrowserVersion = (EditText) findViewById(R.id.editText_browserVersion);
        mEditbrowserVendor = (EditText) findViewById(R.id.editText_browserVendor);
        mEdituserAgent = (EditText) findViewById(R.id.editText_userAgent);
        mEditdisplayWidth = (EditText) findViewById(R.id.editText_displayWidth);
        mEditdisplayHeight = (EditText) findViewById(R.id.editText_displayHeight);
        mEditdisplayPpi = (EditText) findViewById(R.id.editText_displayPpi);
        mEditdiagonalScreenSize = (EditText) findViewById(R.id.editText_diagonalScreenSize);
        mEditconnectionType = (EditText) findViewById(R.id.editText_connectionType);
        mEditconnectionSecure = (EditText) findViewById(R.id.editText_connectionSecure);
        mEditconnectionPort = (EditText) findViewById(R.id.editText_connectionPort);
        mEditapplicationId = (EditText) findViewById(R.id.editText_applicationId);

        SharedPreferences settings = getSharedPreferences(ADOBE_PASS_APPINFO,0);
        mEditAppProfile.setText(settings.getString(APPLICATION_PROFILE,""));
        mEditVisitorId.setText(settings.getString(VISITOR_ID,""));
        mEditAdId.setText(settings.getString(AD_ID,""));
        mEditprimaryHardwareType.setText(settings.getString(PRIMARY_HARDWARE_TYPE,""));
        mEditmodel.setText(settings.getString(MODEL, Build.MODEL));
        mEditversion.setText(settings.getString(VERSION, Build.DEVICE));
        mEditvendor.setText(settings.getString(VENDOR,Build.BRAND));
        mEditmanufacturer.setText(settings.getString(MANUFACTURER,Build.MANUFACTURER));
        mEditosName.setText(settings.getString(OS_NAME, "Android"));
        mEditosFamily.setText(settings.getString(OS_FAMILY,""));
        mEditosVendor.setText(settings.getString(OS_VENDOR,""));
        mEditosVersion.setText(settings.getString(OS_VERSION,Build.VERSION.RELEASE));
        mEditbrowserName.setText(settings.getString(BROWSER_NAME,""));
        mEditbrowserVersion.setText(settings.getString(BROWSER_VERSION,""));
        mEditbrowserVendor.setText(settings.getString(BROWSER_VENDOR,""));
        mEdituserAgent.setText(settings.getString(USER_AGENT,""));
        mEditdisplayWidth.setText(settings.getString(DISPLAY_WIDTH, ""));
        mEditdisplayHeight.setText(settings.getString(DISPLAY_HEIGHT,""));
        mEditdisplayPpi.setText(settings.getString(DISPLAY_PPI,""));
        mEditdiagonalScreenSize.setText(settings.getString(DIAGONAL_SCREEN_SIZE,""));
        mEditconnectionType.setText(settings.getString(CONNECTION_TYPE,""));
        mEditconnectionSecure.setText(settings.getString(CONNECTION_SECURE,""));
        mEditconnectionPort.setText(settings.getString(CONNECTION_PORT,""));
        mEditapplicationId.setText(settings.getString(APPLICATION_ID, BuildConfig.APPLICATION_ID));

    }

    private final OnClickListener btnOkOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String app_profile;
            String visitor_id;
            String ad_id;
            String primaryHardwareType;
            String model;
            String version;
            String vendor;
            String manufacturer;
            String osName;
            String osFamily;
            String osVendor;
            String osVersion;
            String browserName;
            String browserVersion;
            String browserVendor;
            String userAgent;
            String displayWidth;
            String displayHeight;
            String displayPpi;
            String diagonalScreenSize;
            String connectionType;
            String connectionSecure;
            String connectionPort;
            String applicationId;

            app_profile = mEditAppProfile.getText().toString();
            visitor_id = mEditVisitorId.getText().toString();
            ad_id = mEditAdId.getText().toString();
            primaryHardwareType = mEditprimaryHardwareType.getText().toString();
            model = mEditmodel.getText().toString();
            version = mEditversion.getText().toString();
            vendor = mEditvendor.getText().toString();
            manufacturer = mEditmanufacturer.getText().toString();
            osName = mEditosName.getText().toString();
            osFamily = mEditosFamily.getText().toString();
            osVendor = mEditosVendor.getText().toString();
            osVersion = mEditosVersion.getText().toString();
            browserName = mEditbrowserName.getText().toString();
            browserVersion = mEditbrowserVersion.getText().toString();
            browserVendor = mEditbrowserVendor.getText().toString();
            userAgent = mEdituserAgent.getText().toString();
            displayWidth = mEditdisplayWidth.getText().toString();
            displayHeight = mEditdisplayHeight.getText().toString();
            displayPpi = mEditdisplayPpi.getText().toString();
            diagonalScreenSize = mEditdiagonalScreenSize.getText().toString();
            connectionType = mEditconnectionType.getText().toString();
            connectionSecure = mEditconnectionSecure.getText().toString();
            connectionPort = mEditconnectionPort.getText().toString();
            applicationId = mEditapplicationId.getText().toString();

            JSONObject di = new JSONObject();
            try {
                if (!primaryHardwareType.isEmpty()) { di.put(PRIMARY_HARDWARE_TYPE,primaryHardwareType); }
                if (!model.isEmpty()) { di.put(MODEL,model); }
                if (!version.isEmpty()) { di.put(VERSION,version); }
                if (!vendor.isEmpty()) { di.put(VENDOR,vendor); }
                if (!manufacturer.isEmpty()) { di.put(MANUFACTURER,manufacturer); }
                if (!osName.isEmpty()) { di.put(OS_NAME,osName); }
                if (!osFamily.isEmpty()) { di.put(OS_FAMILY,osFamily); }
                if (!osVendor.isEmpty()) { di.put(OS_VENDOR,osVendor); }
                if (!osVersion.isEmpty()) { di.put(OS_VERSION,osVersion); }
                if (!browserName.isEmpty()) { di.put(BROWSER_NAME,browserName); }
                if (!browserVersion.isEmpty()) { di.put(BROWSER_VERSION,browserVersion); }
                if (!browserVendor.isEmpty()) { di.put(BROWSER_VENDOR,browserVendor); }
                if (!userAgent.isEmpty()) { di.put(USER_AGENT,userAgent); }
                if (!displayWidth.isEmpty()) { di.put(DISPLAY_WIDTH,displayWidth); }
                if (!displayHeight.isEmpty()) { di.put(DISPLAY_HEIGHT,displayHeight); }
                if (!displayPpi.isEmpty()) { di.put(DISPLAY_PPI,displayPpi); }
                if (!diagonalScreenSize.isEmpty()) { di.put(DIAGONAL_SCREEN_SIZE, diagonalScreenSize); }
                if (!connectionType.isEmpty()) {di.put(CONNECTION_TYPE, connectionType); }
                if (!connectionSecure.isEmpty()) { di.put(CONNECTION_SECURE,connectionSecure); }
                if (!connectionPort.isEmpty()) { di.put(CONNECTION_PORT,connectionPort); }
                if (!applicationId.isEmpty()) { di.put(APPLICATION_ID,applicationId); }
            } catch (JSONException e) {
                Log.e("Application options",e.getMessage());
            }

            // save preferences
            SharedPreferences settings = getSharedPreferences(ADOBE_PASS_APPINFO,0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(APPLICATION_PROFILE, app_profile);
            editor.putString(VISITOR_ID,visitor_id);
            editor.putString(DEVICE_INFO,encodeDeviceInformation(di));
            editor.putString(AD_ID,ad_id);
            editor.putString(PRIMARY_HARDWARE_TYPE,primaryHardwareType);
            editor.putString(MODEL, model);
            editor.putString(VERSION, version);
            editor.putString(VENDOR,vendor);
            editor.putString(MANUFACTURER,manufacturer);
            editor.putString(OS_NAME, osName);
            editor.putString(OS_FAMILY,osFamily);
            editor.putString(OS_VENDOR,osVendor);
            editor.putString(OS_VERSION,osVersion);
            editor.putString(BROWSER_NAME,browserName);
            editor.putString(BROWSER_VERSION,browserVersion);
            editor.putString(BROWSER_VENDOR,browserVendor);
            editor.putString(USER_AGENT,userAgent);
            editor.putString(DISPLAY_WIDTH, displayWidth);
            editor.putString(DISPLAY_HEIGHT,displayHeight);
            editor.putString(DISPLAY_PPI,displayPpi);
            editor.putString(DIAGONAL_SCREEN_SIZE,diagonalScreenSize);
            editor.putString(CONNECTION_TYPE,connectionType);
            editor.putString(CONNECTION_SECURE,connectionSecure);
            editor.putString(CONNECTION_PORT,connectionPort);
            editor.putString(APPLICATION_ID,applicationId);
            editor.commit();

            Intent result = new Intent(OptionsActivity.this,MainActivity.class);
            result.putExtra(APPLICATION_PROFILE,app_profile);
            result.putExtra(VISITOR_ID,visitor_id);
            result.putExtra(AD_ID,ad_id);
            result.putExtra(DEVICE_INFO,encodeDeviceInformation(di));
            setResult(RESULT_OK, result);
            finish();
        }
    };

    private final OnClickListener btnCancelOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(OptionsActivity.this, MainActivity.class);
            setResult(RESULT_CANCELED,intent);
            finish();
        }
    };

    private String encodeDeviceInformation(JSONObject di) {
        return URLEncoder.encode(Base64.encodeToString(di.toString().getBytes(),Base64.DEFAULT | Base64.NO_WRAP));
    }
}
