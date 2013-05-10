package com.explodingbarrel.android;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.content.*;
import android.provider.Settings.Secure;
import com.unity3d.player.*;

class UnityAndroidDeviceInfo 
{
	public static String GetDeviceID()
	{
		String id = "";
		
		try
		{
			Activity activity = UnityPlayer.currentActivity;
			if( activity != null )
			{
				id = Secure.getString( activity.getContentResolver(), Secure.ANDROID_ID);
			}	
		}
		catch( Exception e )
		{
			
		}
		
		return id;
	}
	
	public static String WifiMacAddress()
	{
		String mac = "";
		
		try
		{
			Activity activity = UnityPlayer.currentActivity;
			if( activity != null )
			{
				WifiManager wifiMan = (WifiManager) activity.getSystemService( Context.WIFI_SERVICE );
				WifiInfo wifiInf = wifiMan.getConnectionInfo();
				mac = wifiInf.getMacAddress();
			}	
		}
		catch( Exception e )
		{
			
		}
		
		return mac;
	}
}