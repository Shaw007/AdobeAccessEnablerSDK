/*
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2020 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by all applicable intellectual property laws,
 * including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.adobe.adobepass.apps.demo.R;

import static com.adobe.adobepass.apps.demo.AdobePassDemoApp.PRODUCTION_URL;
import static com.adobe.adobepass.apps.demo.AdobePassDemoApp.STAGING_URL;
import static com.adobe.adobepass.apps.demo.AdobePassDemoApp.getAppContext;

public class SetSoftwareStatementActivity extends AbstractActivity {

    public static final String ADOBE_PASS_PREFERENCES = "adobePassPreferences";
    public static final String PREF_SOFTWARE_STATEMENT = "softwareStatement";
    public static final String DEFAULT_SW_STATEMENT = "unknown";
    public static final String PREF_REDIRECT_URL = "redirectUrl";
    public static final String PREF_ENVIRONMENT = "envUrl";

    private EditText swStatementText;
    private String redirectText;

    private RadioGroup environmentRadioGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sw_statement);

        Button btnContinue = findViewById(R.id.sw_continue);
        btnContinue.setOnClickListener(btnContinueListener);

        SharedPreferences settings = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0);

        swStatementText = findViewById(R.id.editSoftwareStatement);

        String swText = getAppContext().getResources().getString(R.string.software_statement);

        if (swText.isEmpty() || (DEFAULT_SW_STATEMENT.compareTo(swText) == 0)) {
            swStatementText.setText(settings.getString(PREF_SOFTWARE_STATEMENT, ""));
        } else {
            swStatementText.setText(swText);
        }

        redirectText = getResources().getString(R.string.redirect_uri);

        environmentRadioGroup = findViewById(R.id.radio_group_environment);

        String useURL = settings.getString(PREF_ENVIRONMENT, PRODUCTION_URL);
        if (PRODUCTION_URL.equals(useURL)) {
            environmentRadioGroup.check(R.id.radioButtonProd);
        } else {
            environmentRadioGroup.check(R.id.radioButtonStaging);
        }
    }

    private final View.OnClickListener btnContinueListener = new View.OnClickListener() {
        public void onClick(View view) {
            // save preferences
            SharedPreferences.Editor editor = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0).edit();

            String swStatement = swStatementText.getText().toString();
            editor.putString(PREF_SOFTWARE_STATEMENT, swStatement);

            String redirectUrl = redirectText;
            editor.putString(PREF_REDIRECT_URL, redirectUrl);

            String useURL = PRODUCTION_URL;
            if (environmentRadioGroup.getCheckedRadioButtonId() == R.id.radioButtonStaging) {
                useURL = STAGING_URL;
            }
            editor.putString(PREF_ENVIRONMENT, useURL);

            editor.commit();

            Intent result = new Intent(SetSoftwareStatementActivity.this, MainActivity.class);

            result.putExtra(PREF_SOFTWARE_STATEMENT, swStatement);
            result.putExtra(PREF_REDIRECT_URL, redirectUrl);
            result.putExtra(PREF_ENVIRONMENT, useURL);

            setResult(RESULT_OK, result);

            finish();
        }
    };
}
