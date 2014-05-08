package com.explodingbarrel.facebook;

import com.explodingbarrel.iap.MainActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Base64;
import android.content.Intent;
import android.content.pm.*;
import android.webkit.WebView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.Gravity;

import com.unity3d.player.*;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.security.MessageDigest;

import org.json.JSONObject;


public class FacebookIAPMainActivity extends com.explodingbarrel.iap.MainActivity
{
    // Debug tag, for logging
    private static final String TAG = "FacebookIAP";

    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	 if (!isTaskRoot()) {
             Intent intent = getIntent();
             String action = intent.getAction();
             if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null && action.equals(Intent.ACTION_MAIN)) {
                 finish();
                 return;
             }
         }
    	 
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onResume()
    {
    	try
    	{
    		super.onResume();
    	}
    	catch( Exception e )
		{
			Log.d(TAG, "onResume : Failed - " + e.getMessage() );
		}
    }
    
    @Override
    protected void onPause()
	{ 
		 super.onPause();
		 this.WebViewPopupClose();
	} 
    
    boolean WebViewShowFullscreen( String url, String config )
    {
    	Log.d(TAG, "WebViewShowFullscreen : url = " + url + " config = " + config );
    	
    	boolean valid = false;
    	
    	Intent webViewIntent = new Intent(this, WebViewFullScreenActivity.class);
    	if( webViewIntent != null )
    	{
    		Log.d(TAG, "WebViewShowFullscreen : Pending - Starting full screen webview activity" );
    		webViewIntent.putExtra("url", url);
    		webViewIntent.putExtra("config", config);
    		startActivity( webViewIntent );
    		valid = true;
    	}
    	else
    	{
    		Log.d(TAG, "WebViewShowFullscreen : Failed - Couldn't load the fullscreenwebview from the layout webviewfullscreen" );
    	}

    	return valid;
    }
    
    private WebViewDialog ActiveWebViewPopup = null; 
    
    public static float clamp(float val, float min, float max) 
    {
        return Math.max(min, Math.min(max, val));
    }
    
    boolean WebViewPopup( final String url, final int targetWidth, final float normalizedX, final float normalizedY, final float normalizedWidth, final float normalizedHeight )
    {	
    	Log.d(TAG, "WebViewPopup : url = " + url + " normalizedX = " + normalizedX + " normalizedY = " + normalizedY + " normalizedWidth = " + normalizedWidth + " normalizedHeight = " + normalizedHeight );
    	
    	this.runOnUiThread( new Runnable()
    	{
    		 public void run()
             {				
    			 DisplayMetrics metrics = new DisplayMetrics();
    			 getWindowManager().getDefaultDisplay().getMetrics(metrics);
    			 
    			 float clampedNormalizedX = clamp( normalizedX, 0.0f, 1.0f );
    			 float clampedNormalizedY = clamp( normalizedY, 0.0f, 1.0f );
    			 float clampedNormalizedWidth = clamp( normalizedWidth, 0.0f, 1.0f );
    			 float clampedNormalizedHeight = clamp( normalizedHeight, 0.0f, 1.0f );
    			 
    			 Log.d(TAG, "WebViewPopup : Screen Width = " + metrics.widthPixels + " Screen Height = " + metrics.heightPixels );
				
    			 float x = clampedNormalizedX * metrics.widthPixels;
    			 float y = clampedNormalizedY * metrics.heightPixels;
    			 float width = clampedNormalizedWidth * metrics.widthPixels;
    			 float height = clampedNormalizedHeight * metrics.heightPixels;
    			 
    			 Log.d(TAG, "WebViewPopup : url = " + url + " x = " + (int)x + " y = " + (int)y + " width = " + (int)width + " height = " + (int)height );
    			 
    			 FacebookIAPMainActivity.this.ActiveWebViewPopup = new WebViewDialog( FacebookIAPMainActivity.this, url, targetWidth, (int)x, (int)y, (int)width, (int)height );
    			 Window window = FacebookIAPMainActivity.this.ActiveWebViewPopup.getWindow();
    			 window.setFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL );
    			 window.clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
    			 window.setBackgroundDrawableResource(android.R.color.transparent);
    		     WindowManager.LayoutParams wmlp = window.getAttributes();
    		     wmlp.gravity = Gravity.TOP | Gravity.LEFT;
    		     wmlp.x = (int)x;
    		     wmlp.y = (int)y;
    			 FacebookIAPMainActivity.this.ActiveWebViewPopup.show();
             }
    	} );
    	
    	return true;
    }
    
    boolean WebViewPopupClose()
    {
    	Log.d(TAG, "WebViewPopupClose" );
    	boolean valid = false;
    	if( this.ActiveWebViewPopup != null )
    	{
    		valid = true;
    		this.runOnUiThread( new Runnable()
    		{
    			public void run()
    			{	
    				FacebookIAPMainActivity.this.ActiveWebViewPopup.dismiss();
		    	}
    		});
    	}
    	
    	return valid;
    }


    Bundle ConvertJSONToBundle( String json )
    {
        Bundle bundle = new Bundle();

        try
        {
            JSONObject jsonObj = new JSONObject( json );
            if( jsonObj != null )
            {
                Iterator it = jsonObj.keys();
                while( it.hasNext() == true )
                {
                    String key = (String)it.next();
                    Object value = jsonObj.get( key );
                    if( value instanceof String )
                    {
                        bundle.putString( key, (String) value );
                    }
                }
            }
        }
        catch (org.json.JSONException ex)
        {
        }

        return bundle;
    }
}
