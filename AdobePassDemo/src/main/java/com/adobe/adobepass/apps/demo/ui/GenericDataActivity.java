/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2014 Adobe Systems Incorporated
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.adobe.adobepass.apps.demo.R;

/**
 * Created by tanase on 12/18/14.
 */
public class GenericDataActivity extends AbstractActivity {

    private static final String GENERIC_DATA_PREFERENCES = "genericDataPreferences";
    private static final String PREF_AUTHN_GENERIC_DATA = "authnGenericData";
    private static final String PREF_AUTHZ_GENERIC_DATA = "authzGenericData";
    private static final String PREF_FORCE_AUTHN = "forceAuthn";

    private static final String DEFAULT_GENERIC_DATA = "{\"email\": \"hashed_email\"}";

    private EditText genericDataEdit;
    private EditText forceAuthnEdit;

    private String entitlementMethod;
    private String authzResourceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.generic_data);

        Button btnOk = (Button) findViewById(R.id.btn_generic_data_ok);
        Button btnCancel = (Button) findViewById(R.id.btn_generic_data_cancel);

        btnOk.setOnClickListener(btnOkOnClickListener);
        btnCancel.setOnClickListener(btnCancelOnClickListener);

        genericDataEdit = (EditText) findViewById(R.id.generic_data_edit);
        forceAuthnEdit = (EditText) findViewById(R.id.force_authn_edit);

        Bundle extras = getIntent().getExtras();
        entitlementMethod = extras.getString("entitlement_method");
        authzResourceId = extras.getString("resource_id");

        // read data from shared preferences
        SharedPreferences settings = getSharedPreferences(GENERIC_DATA_PREFERENCES, 0);
        if (entitlementMethod.equals("getAuthenticationWithData")) {
            String genericData = settings.getString(PREF_AUTHN_GENERIC_DATA, DEFAULT_GENERIC_DATA);
            boolean forceAuthn = settings.getBoolean(PREF_FORCE_AUTHN, true);

            genericDataEdit.setText(genericData);
            forceAuthnEdit.setText(String.valueOf(forceAuthn));
        } else {
            String genericData = settings.getString(PREF_AUTHZ_GENERIC_DATA, DEFAULT_GENERIC_DATA);

            genericDataEdit.setText(genericData);

            TextView forceAuthnLabel = (TextView) findViewById(R.id.force_authn_label);
            forceAuthnLabel.setVisibility(View.INVISIBLE);

            forceAuthnEdit.setVisibility(View.INVISIBLE);
        }
    }

    private final View.OnClickListener btnOkOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            String genericData = genericDataEdit.getText().toString();
            String forceAuthn = forceAuthnEdit.getText().toString();

            // save preferences
            SharedPreferences settings = getSharedPreferences(GENERIC_DATA_PREFERENCES, 0);
            SharedPreferences.Editor editor = settings.edit();
            if (entitlementMethod.equals("getAuthenticationWithData")) {
                editor.putString(PREF_AUTHN_GENERIC_DATA, genericData);
                editor.putBoolean(PREF_FORCE_AUTHN, Boolean.valueOf(forceAuthn));
            } else {
                editor.putString(PREF_AUTHZ_GENERIC_DATA, genericData);
            }
            editor.commit();

            Intent result = new Intent(GenericDataActivity.this, MainActivity.class);
            result.putExtra("entitlement_method", entitlementMethod);
            result.putExtra("generic_data", genericData);
            if (entitlementMethod.equals("getAuthenticationWithData")) {
                result.putExtra("force_authn", forceAuthn);
            } else {
                result.putExtra("resource_id", authzResourceId);
            }
            setResult(RESULT_OK, result);
            finish();
        }
    };

    private final View.OnClickListener btnCancelOnClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            Intent result = new Intent(GenericDataActivity.this, MainActivity.class);
            setResult(RESULT_CANCELED, result);
            finish();
        }
    };
}