/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo;

import android.app.Application;
import android.content.Context;

import com.adobe.adobepass.accessenabler.api.AccessEnabler;

public class AdobePassDemoApp extends Application {
    public static String STAGING_URL;
    public static String PRODUCTION_URL;

    private static final String LOG_TAG = "AdobePassDemoApp";
    private static AccessEnabler accessEnabler;

    public static Context getAppContext() {
        return applicationContext;
    }

    private static Context applicationContext;

    public static void setAccessEnabler(AccessEnabler accessEnabler) {
        AdobePassDemoApp.accessEnabler = accessEnabler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AdobePassDemoApp.applicationContext = getApplicationContext();

        STAGING_URL = getResources().getString(R.string.sp_url_staging);
        PRODUCTION_URL = getResources().getString(R.string.sp_url_production);
    }

    public static AccessEnabler getAccessEnablerInstance() { return AdobePassDemoApp.accessEnabler; }
}
