package com.explodingbarrel.android;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.content.*;
import android.provider.Settings.Secure;
import android.util.Log;

import com.unity3d.player.*;

class UnityAndroidDeviceInfo 
{
	// Debug tag, for logging
    private static final String TAG = "UnityAndroidDeviceInfo";
	
	public static String GetDeviceID()
	{
		Log.d(TAG, "DeviceInfo GetDeviceID");
		
		String id = "";
		
		try
		{
			Activity activity = UnityPlayer.currentActivity;
			if( activity != null )
			{
				id = Secure.getString( activity.getContentResolver(), Secure.ANDROID_ID);
				Log.d(TAG, "DeviceInfo : Success - id = " + id );
			}
			else
			{
				Log.d(TAG, "DeviceInfo : Failed - Couldn't get the current activity from Unity");
			}
		}
		catch( Exception e )
		{
			Log.d(TAG, "DeviceInfo : Failed - " + e.getMessage() );
		}
		
		return id;
	}
	
	public static String WifiMacAddress()
	{
		Log.d(TAG, "DeviceInfo WifiMacAddress");
		
		String mac = "";
		
		try
		{
			Activity activity = UnityPlayer.currentActivity;
			if( activity != null )
			{
				WifiManager wifiMan = (WifiManager) activity.getSystemService( Context.WIFI_SERVICE );
				WifiInfo wifiInf = wifiMan.getConnectionInfo();
				mac = wifiInf.getMacAddress();
				
				Log.d(TAG, "DeviceInfo : Success - mac address = " + mac );
			}
			else
			{
				Log.d(TAG, "DeviceInfo : Failed - Couldn't get the current activity from Unity");
			}
		}
		catch( Exception e )
		{
			Log.d(TAG, "DeviceInfo : Failed - " + e.getMessage() );
		}
		
		return mac;
	}
}