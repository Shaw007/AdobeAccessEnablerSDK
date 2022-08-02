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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.adobe.adobepass.apps.demo.R;
import com.adobe.adobepass.apps.demo.ui.AbstractActivity;

import java.util.ArrayList;
import java.util.List;

import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;

public class StorageViewerActivity extends AbstractActivity {

    private TreeViewList treeView;

    private TreeStateManager<Long> manager = null;
    private boolean collapsible;

    private StorageHelper storageHelper;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storageHelper = new StorageHelper(getApplicationContext());

        changeActivityTitle();
        setContentView(R.layout.storage_viewer);

        refreshTreeView();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putSerializable("treeManager", manager);
        outState.putBoolean("collapsible", this.collapsible);
        super.onSaveInstanceState(outState);
    }

    protected final void setCollapsible(final boolean newCollapsible) {
        this.collapsible = newCollapsible;
        treeView.setCollapsible(this.collapsible);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.storage_viewer_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.expand_all_menu_item) {
            manager.expandEverythingBelow(null);
        } else if (item.getItemId() == R.id.collapse_all_menu_item) {
            manager.collapseChildren(null);
        }

        return true;
    }

    private void refreshTreeView() {
        List<StorageHelper.TreeNode> nodes = storageHelper.readStorage();

        manager = new InMemoryTreeStateManager<>();
        final TreeBuilder<Long> treeBuilder = new TreeBuilder<>(manager);
        for (StorageHelper.TreeNode node : nodes) {
            treeBuilder.sequentiallyAddNextNode(node.getId(), node.getLevel());
        }

        treeView = findViewById(R.id.mainTreeView);
        List<String> labels = new ArrayList<>();
        int maxLevel = 0;
        for (StorageHelper.TreeNode node : nodes) {
            labels.add(node.getLabel());
            if (node.getLevel() > maxLevel)
                maxLevel = node.getLevel();
        }
        treeView.setAdapter(new SimpleStandardAdapter(this, manager, maxLevel + 1, labels));

        setCollapsible(true);
    }

    void changeActivityTitle() {
        setTitle("Storage v" + storageHelper.storageVersion + " (" + storageHelper.storageType +")");
    }
}
