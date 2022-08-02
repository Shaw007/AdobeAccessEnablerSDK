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

package com.adobe.adobepass.apps.demo.ui.storageviewer;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityCompat;

public class StorageHelper {

    private static final String LOG_TAG = StorageHelper.class.getName();

    static class TreeNode {
        private long id;
        private int level;
        private String label;

        public TreeNode(int id, int level, String label) {
            this.id = id;
            this.level = level;
            this.label = label;
        }

        public long getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }

        public String getLabel() {
            return label;
        }

        public String toString() {
            return id + " | " + level + " | " + label;
        }
    }

    public final int storageVersion = 6;
    private String storageFilePath;
    public String storageType;

    public StorageHelper(Context applicationContext) {
        final String DATABASE_FILENAME = ".adobepassdb";

        File databaseFile;

        boolean isExternalStorageLegacy;

        // External scoped storage was introduced in Android 10 a.k.a Android Q, API Level 29
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isExternalStorageLegacy = Environment.isExternalStorageLegacy();
        } else {
            isExternalStorageLegacy = true;
        }

        if (isExternalStorageLegacy &&
                ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "Will use external storage. SSO is available!");

            databaseFile = Environment.getExternalStorageDirectory();

            storageType = "SSO available";
        } else {
            Log.d(LOG_TAG, "Will use application's external scoped storage. SSO is unavailable!");

            databaseFile = applicationContext.getExternalFilesDir(null);

            storageType = "SSO unavailable";
        }

        if (databaseFile == null) {
            throw new RuntimeException("Cannot access storage file because it is a null reference!");
        }

        storageFilePath = databaseFile.getPath() + File.separator + DATABASE_FILENAME + "_" + storageVersion;
    }

    public void clearStorage() {
        try {
            File storageFile = new File(storageFilePath);

            if (storageFile.exists() && !storageFile.delete()) {
                Log.d(LOG_TAG, "Error clearing storage");
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error clearing storage because of: " + e.toString());
        }
    }

    public List<TreeNode> readStorage() {
        List<TreeNode> nodes = new ArrayList<>();
        Map storageMap;

        try {
            FileInputStream fileIn = new FileInputStream(storageFilePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);

            storageMap = (Map) in.readObject();

            in.close();
            fileIn.close();

            traverseTree(storageMap, 0, nodes);
        } catch(Exception e) {
            Log.d(LOG_TAG, "Error while reading from storage: " + e.toString());
        }

        if (nodes.size() == 0) {
            nodes.add(new TreeNode(0, 0, "Empty storage"));
        }

        return nodes;
    }

    private void traverseTree(Object currentNode, int level, List<TreeNode> nodes) {
        final int MAX_STRING_LENGTH = 100;

        if (currentNode == null) {
            return;
        }

        if (isPrimitiveValue(currentNode)) {
            nodes.add(new TreeNode(nodes.size(), level, String.valueOf(currentNode)));
        } else if (currentNode instanceof Map) {
            Map map = (Map) currentNode;

            for (Object key : map.keySet()) {
                if (!(key instanceof String)) {
                    continue;
                }

                Object value = map.get(key);

                if (value == null || (isPrimitiveValue(value) && String.valueOf(value).length() < MAX_STRING_LENGTH)) {
                    nodes.add(new TreeNode(nodes.size(), level, key + ": " + (value != null ? String.valueOf(value) : "")));
                } else {
                    nodes.add(new TreeNode(nodes.size(), level, String.valueOf(key)));

                    traverseTree(value, level + 1, nodes);
                }
            }
        } else if (currentNode instanceof List) {
            List list = (List) currentNode;

            for (int i = 0; i < list.size(); i++) {
                Object value = list.get(i);

                if (value == null || (isPrimitiveValue(value) && String.valueOf(value).length() < MAX_STRING_LENGTH)) {
                    nodes.add(new TreeNode(nodes.size(), level, i + ": " + (value != null ? String.valueOf(value) : "")));
                } else {
                    nodes.add(new TreeNode(nodes.size(), level, String.valueOf(i)));

                    traverseTree(value, level + 1, nodes);
                }
            }
        }
    }

    private boolean isPrimitiveValue(Object value) {
        return value instanceof String || value instanceof Integer || value instanceof Long || value instanceof Boolean;
    }
}
