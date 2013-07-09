package com.explodingbarrel.android;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.content.*;
import android.provider.Settings.Secure;
import android.util.Log;

import java.security.MessageDigest;
import android.util.Base64;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

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
                if( wifiMan != null )
                {
                    WifiInfo wifiInf = wifiMan.getConnectionInfo();
                    if( wifiInf != null )
                    {
                        String candidate = wifiInf.getMacAddress();
                        if( candidate != null )
                        {
                            mac = candidate;
                            Log.d(TAG, "DeviceInfo : Success - mac address = " + mac );
                        }
                        else
                        {
                            Log.d(TAG, "DeviceInfo : Failed - Couldn't get the WifiInfo.getMacAddress return null");
                        }
                    }
                    else
                    {
                        Log.d(TAG, "DeviceInfo : Failed - Couldn't get the WifiInfo from the WifiManager");
                    }
                }
                else
                {
                    Log.d(TAG, "DeviceInfo : Failed - Couldn't get the WifiManager");
                }
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
	
	public static String getHashedPackageInfo( String bundleID, int flags )
	{
		String hash = "";
		try 
	    {
			Activity activity = UnityPlayer.currentActivity;
			if( activity != null )
			{
		        PackageInfo info = activity.getPackageManager().getPackageInfo( bundleID, flags );
		        for (Signature signature : info.signatures) 
		        {
		        	MessageDigest md = MessageDigest.getInstance("SHA");
		        	md.update(signature.toByteArray());
		        	hash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
		        }
			}
	    } 
	    catch (Exception e) {}
	    
	    return hash;
	}
}