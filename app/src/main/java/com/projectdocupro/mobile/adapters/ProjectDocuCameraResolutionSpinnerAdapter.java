package com.projectdocupro.mobile.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.projectdocupro.mobile.managers.AppConstantsManager;
import com.projectdocupro.mobile.managers.SharedPrefsManager;

public class ProjectDocuCameraResolutionSpinnerAdapter extends ArrayAdapter <String> implements OnItemSelectedListener {
	private Context context = null;
	private String [] autoGpsDistanceStrings = null;

	public ProjectDocuCameraResolutionSpinnerAdapter(Context context, String [] autoGpsDistanceStrings) {
		super(context, android.R.layout.simple_spinner_item, autoGpsDistanceStrings);
		
		this.context = context;
		this.autoGpsDistanceStrings = autoGpsDistanceStrings;
	}
	
	@Override
	public int getCount () {
		return autoGpsDistanceStrings.length;
	}
	
	@Override
	public View getDropDownView (int position, View convertView, ViewGroup parent) {
		TextView textView = new TextView (context);
		
		textView.setPadding(10, 20, 10, 20);
		textView.setText(autoGpsDistanceStrings[position]);
		
		return textView;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		SharedPrefsManager projectDocuDatabaseManager = new SharedPrefsManager (context);
		String selectedValue=autoGpsDistanceStrings[position];
		if(position>0&&!selectedValue.equals("")){
			selectedValue= selectedValue.split("%")[0];
		}else{
			selectedValue="100";
		}
		projectDocuDatabaseManager.setStringValue(AppConstantsManager.USER_SELECTED_CAMERA_RESOLUTION,selectedValue);

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}
}
