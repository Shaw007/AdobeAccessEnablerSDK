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

package com.adobe.adobepass.apps.demo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.EditText;
import com.adobe.adobepass.apps.demo.R;
import static com.adobe.adobepass.apps.demo.AdobePassDemoApp.*;

public class PreferencesActivity extends AbstractActivity {

    public static final String ADOBE_PASS_PREFERENCES = "adobePassPreferences";
    public static final String PREF_SIGNING_CREDENTIAL = "signingCredential";
    public static final String PREF_USE_HTTPS = "useHttps";

    private RadioGroup signingCredentialRadioGroup;
    private CheckBox httpsCheckbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preferences);

        httpsCheckbox = (CheckBox) findViewById(R.id.checkbox_https);

        signingCredentialRadioGroup = (RadioGroup) findViewById(R.id.radio_group_signing_credential);
        Button btnOk = (Button) findViewById(R.id.btn_preferences_ok);
        Button btnCancel = (Button) findViewById(R.id.btn_preferences_cancel);

        btnOk.setOnClickListener(btnOkOnClickListener);
        btnCancel.setOnClickListener(btnCancelOnClickListener);

        // read url from shared preferences (default value is staging)
        SharedPreferences settings = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0);
        int signingCredential = settings.getInt(PREF_SIGNING_CREDENTIAL, 2048);
        boolean useHttps = settings.getBoolean(PREF_USE_HTTPS, true);

        httpsCheckbox.setChecked(useHttps);

    }

    private final OnClickListener btnOkOnClickListener = new OnClickListener() {
        public void onClick(View view) {

            int signingCredentialRadioButtonId = signingCredentialRadioGroup.getCheckedRadioButtonId();
            View signingCredentialRadioButton = signingCredentialRadioGroup.findViewById(signingCredentialRadioButtonId);
            int signingCredentialIndex = signingCredentialRadioGroup.indexOfChild(signingCredentialRadioButton);

            int signingCredential;
            switch (signingCredentialIndex) {
                case 0: signingCredential = 1024; break;
                case 1: signingCredential = 2048; break;
                default: signingCredential = 2048; break;
            }

            boolean useHttps = httpsCheckbox.isChecked();

            // save preferences
            SharedPreferences settings = getSharedPreferences(ADOBE_PASS_PREFERENCES, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(PREF_SIGNING_CREDENTIAL, signingCredential);
            editor.putBoolean(PREF_USE_HTTPS, useHttps);
            editor.commit();

            Intent result = new Intent(PreferencesActivity.this, MainActivity.class);
            result.putExtra("signing_credential", signingCredential);
            result.putExtra("use_https", useHttps);
            setResult(RESULT_OK, result);
            finish();
        }
    };

    private final OnClickListener btnCancelOnClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent intent = new Intent(PreferencesActivity.this, MainActivity.class);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    };
}
