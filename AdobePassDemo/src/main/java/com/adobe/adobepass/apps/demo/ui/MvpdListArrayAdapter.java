/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2015 Adobe Systems Incorporated
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.adobe.adobepass.apps.demo.R;

import java.util.ArrayList;
import java.util.List;

public class MvpdListArrayAdapter extends ArrayAdapter<MvpdListItem> {
	private int resource;
	private List<MvpdListItem> filteredData = null;
	private List<MvpdListItem> originalData = null;

	public MvpdListArrayAdapter(Context context, int textViewResourceId, List<MvpdListItem> objects) {
		super(context, textViewResourceId, objects);

		this.resource = textViewResourceId;
		this.filteredData = objects;
		this.originalData = objects;
	}

	@Override
	public int getCount() {
		return filteredData.size();
	}

	@Override
	public MvpdListItem getItem(int position) {
		return filteredData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout mvpdItemView;

		MvpdListItem mvpdListItem = getItem(position);

		if (convertView == null) {
			mvpdItemView = new LinearLayout(getContext());
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			inflater.inflate(resource, mvpdItemView, true);
		} else {
			mvpdItemView = (LinearLayout) convertView;
		}

		TextView mvpdNameTv = (TextView) mvpdItemView.findViewById(R.id.mvpd_name);
		mvpdNameTv.setText(mvpdListItem.getMvpd().getDisplayName());

		return mvpdItemView;
	}

	@Override
	public Filter getFilter() {

		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				filteredData = (List<MvpdListItem>) results.values;
				notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				String filterString = constraint.toString().toLowerCase();
				FilterResults results = new FilterResults();

				int count = originalData.size();
				final ArrayList<MvpdListItem> resultList = new ArrayList<MvpdListItem>(count);

				for (MvpdListItem currentItem : originalData) {
					String mvpdId = currentItem.getMvpd().getId();
					if (mvpdId.toLowerCase().contains(filterString)) {
						resultList.add(currentItem);
					}
				}

				results.values = resultList;
				results.count = resultList.size();

				return results;
			}
		};
	}

}
