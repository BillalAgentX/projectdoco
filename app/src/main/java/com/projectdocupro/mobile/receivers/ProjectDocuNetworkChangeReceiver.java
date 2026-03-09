package com.projectdocupro.mobile.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.projectdocupro.mobile.service.SyncLocalPhotosService;

public class ProjectDocuNetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager (context);


        if (mobileNetworkIsConnected(context)) {
//    		Intent serviceIntent = new Intent(context, SyncLocalPhotosService.class);
//    		context.stopService(serviceIntent);
//			startBackgroundTask(context);
        } else {
//    		if ((Integer) projectDocuDatabaseManager.selectDataFromPreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_ALLOW_SYNCHRONIZATION_IN_BACKGROUND) == 1 || ProjectDocuMainActivity.projectDocuMainActivity != null) {
//	    		Intent serviceIntent = new Intent(context, SyncLocalPhotosService.class);
//			    serviceIntent.putExtra("projectId","44");
//	    		context.startService(serviceIntent);
//			if(ProjectDocuUtilities.isNetworkConnected(context))
//			startBackgroundTask(context);
//			}
        }
    }

	public void startBackgroundTask(Context context) {
		SyncLocalPhotosService.enqueueWork(context);
	}



	private boolean mobileNetworkIsConnected (Context context) {
		try {
			return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean wlanIsConnected (Context context) {
		try {
			return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		} catch (Exception e) {
			return false;
		}
	}
}
