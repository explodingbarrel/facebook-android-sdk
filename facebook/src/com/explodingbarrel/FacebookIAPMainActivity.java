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

import com.unity3d.player.*;
import com.facebook.*;
import com.facebook.widget.WebDialog;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult( requestCode, resultCode, data );
        if( this.CurrentSession != null )
        {
        	this.CurrentSession.onActivityResult(this, requestCode, resultCode, data);
        }
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
	    //try 
	    //{
	    //    PackageInfo info = getPackageManager().getPackageInfo("com.kabam.ff6android", PackageManager.GET_SIGNATURES);
	    //    for (Signature signature : info.signatures) 
	    //    {
	    //    	MessageDigest md = MessageDigest.getInstance("SHA");
	    //    	md.update(signature.toByteArray());
	    //    	Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
	    //    }
	    //} 
	    //catch (Exception e) {}
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
    
    boolean WebViewShowFullscreen( String url, String tabs, int targetWidth )
    {
    	Log.d(TAG, "WebViewShowFullscreen : url = " + url + " tabs = " + tabs + " targetWidth = " + targetWidth );
    	
    	boolean valid = false;
    	
    	Intent webViewIntent = new Intent(this, WebViewFullScreenActivity.class);
    	if( webViewIntent != null )
    	{
    		Log.d(TAG, "WebViewShowFullscreen : Pending - Starting full screen webview activity" );
    		webViewIntent.putExtra("url", url);
    		webViewIntent.putExtra("tabs", tabs);
    		webViewIntent.putExtra("targetWidth", targetWidth);
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
    
    boolean WebViewPopup( final String url, final int targetWidth, final float normalizedX, final float normalizedY, final float normalizedWidth, final float normalizedHeight )
    {	
    	Log.d(TAG, "WebViewPopup : url = " + url + " normalizedX = " + normalizedX + " normalizedY = " + normalizedY + " normalizedWidth = " + normalizedWidth + " normalizedHeight = " + normalizedHeight );
    	
    	this.runOnUiThread( new Runnable()
    	{
    		 public void run()
             {				
    			 DisplayMetrics metrics = new DisplayMetrics();
    			 getWindowManager().getDefaultDisplay().getMetrics(metrics);
    			 
    			 Log.d(TAG, "WebViewPopup : Screen Width = " + metrics.widthPixels + " Screen Height = " + metrics.heightPixels );
				
    			 float x = normalizedX * metrics.widthPixels;
    			 float y = normalizedY * metrics.heightPixels;
    			 float width = normalizedWidth * metrics.widthPixels;
    			 float height = normalizedHeight * metrics.heightPixels;
    			 
    			 Log.d(TAG, "WebViewPopup : url = " + url + " x = " + x + " y = " + y + " width = " + width + " height = " + height );
    			 
    			 FacebookIAPMainActivity.this.ActiveWebViewPopup = new WebViewDialog( FacebookIAPMainActivity.this, url, targetWidth, (int)x, (int)y, (int)width, (int)height );
    			 Window window = FacebookIAPMainActivity.this.ActiveWebViewPopup.getWindow();
    			 window.setFlags( WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL );
    			 window.clearFlags( WindowManager.LayoutParams.FLAG_DIM_BEHIND );
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
    
    private String AppId;
    private Session CurrentSession = null;
    private boolean Initialized = false;

    boolean FacebookInit( String appId )
    {
        Log.d(TAG, "Facebook Init : appId = " + appId );

        this.Initialized = false;
        this.AppId = appId;
        
        if( this.CurrentSession == null )
        {
            this.CurrentSession = new Session.Builder( this ).setApplicationId( appId ).build();
            if( this.CurrentSession != null )
            {
                Session.setActiveSession( this.CurrentSession );
                this.Initialized = true;
                Log.d(TAG, "Facebook Init : Success" );
            }
            else
            {
                Log.d(TAG, "Facebook Init : Failed - Could not construct Session using Session.Builder" );
            }
        }
        else
        {
            Log.d(TAG, "Facebook Init : Failed - CurrentSession already exists" );
        }

        return this.Initialized;
    }
    
    private class OpenForReadSessionStateCallback implements Session.StatusCallback
    {
    	private FacebookIAPMainActivity Activity;
        private String MessageHandler;
        private String SuccessMsg;
        private String FailMsg;
    
        public OpenForReadSessionStateCallback( FacebookIAPMainActivity activity, String messageHandler, String successMsg, String failMsg )
        {
        	this.Activity = activity;
            this.MessageHandler = messageHandler;
            this.SuccessMsg = successMsg;
            this.FailMsg = failMsg;
        }
    
        @Override
        public void call(Session session, SessionState state, Exception exception)
        {
            if( session.isOpened() == true )
            {
                String accessToken = session.getAccessToken();
                UnityPlayer.UnitySendMessage( this.MessageHandler, this.SuccessMsg, accessToken );
                Log.d(TAG, "Facebook Login : Success accessToken = " + accessToken );
            }
            else if( session.isClosed() == true )
            {
                String error = "Unknown Reason";
                if( state == SessionState.CLOSED_LOGIN_FAILED )
                {
                    boolean cancelled = false;
                    if( exception != null )
                    {
                        if( exception instanceof FacebookOperationCanceledException )
                        {
                            cancelled = true;
                        }
                        error = exception.getMessage();
                    }
                    UnityPlayer.UnitySendMessage( this.MessageHandler, this.FailMsg, String.valueOf( cancelled ) );
                }
                else
                {
                    Log.d(TAG, "Facebook Login : Failed - exception = " + exception);
                    UnityPlayer.UnitySendMessage( this.MessageHandler, this.FailMsg, "false" );
                }
                Log.d(TAG, "Facebook Login : Failed - " + error );
                
                //This has invalidated the session because the facebook API is bad. Clear it so that it will be recreated.
                this.Activity.CurrentSession = null;
            }
        }
    };

    boolean FacebookLogin( final String scope, final boolean allowUI, final String messageHandler, final String authorizeSuccess, final String authorizeFail )
    {
        Log.d( TAG, "Facebook Login : scope = " + scope + " allowUI = " + allowUI );

        boolean success = false;
        
        if( ( this.Initialized == true ) && ( this.CurrentSession == null ) )
        {
        	Log.d( TAG, "Facebook Login : Session was invalidated and is begin recreated" );
        	this.CurrentSession = new Session.Builder( this ).setApplicationId( this.AppId  ).build();
            if( this.CurrentSession != null )
            {
                Session.setActiveSession( this.CurrentSession );
            }
        }
    
        if( this.CurrentSession != null )
        {
            this.runOnUiThread( new Runnable()
            {
                public void run()
                {
                	Log.d(TAG, "Facebook Login : Starting state - " + FacebookIAPMainActivity.this.CurrentSession.getState() );	
                	if( FacebookIAPMainActivity.this.CurrentSession.isOpened() == true )
                	{
                		String accessToken = FacebookIAPMainActivity.this.CurrentSession.getAccessToken();
                		Log.d(TAG, "Facebook Login : Already Logged In accessToken = " + accessToken );	
                        UnityPlayer.UnitySendMessage( messageHandler, authorizeSuccess, accessToken );
                	}
                	else
                	{
	                    Session.OpenRequest openRequest = new Session.OpenRequest( FacebookIAPMainActivity.this );
	                    if( openRequest != null )
	                    {
	                        List<String> permissions = Arrays.asList( scope.split( "," ) );
	                        openRequest.setPermissions( permissions );
	                        if( allowUI == true )
	                        {
	                            openRequest.setLoginBehavior( SessionLoginBehavior.SSO_WITH_FALLBACK );
	                        }
	                        else
	                        {
	                            openRequest.setLoginBehavior( SessionLoginBehavior.SSO_ONLY );
	                        }
	                        openRequest.setCallback( new OpenForReadSessionStateCallback( FacebookIAPMainActivity.this, messageHandler, authorizeSuccess, authorizeFail ) );
	                        FacebookIAPMainActivity.this.CurrentSession.openForRead( openRequest );
	                        
	                        Log.d(TAG, "Facebook Login : Pending" );
	                    }
	                    else
	                    {
	                        Log.d(TAG, "Facebook Login : Failed - Could not construct an OpenRequest" );
	                    }
                	}
                }
            } );
            success = true;
        }
        else
        {
            Log.d(TAG, "Facebook Login : Failed - CurrentSession does not exist. Call FacebookInit before FacebookLogin" );
        }

        return success;
    }

    boolean FacebookLogout()
    {
        Log.d( TAG, "Facebook Logout" );

        boolean success = false;

        if( this.CurrentSession != null )
        {
            this.CurrentSession.close();
            Log.d( TAG, "Facebook Logout : Success" );
            success = true;
        }
        else
        {
            Log.d(TAG, "Facebook Logout : Failed - CurrentSession does not exist. Call FacebookInit before FacebookLogout" );
        }

        return success;
    }

    boolean FacebookUI( final String action, final String params, final String messageHandler, final String successMsg, final String failMsg )
    {
        Log.d(TAG, "Facebook UI : action = " + action + " params = " + params );

        boolean success = false;

        if( this.CurrentSession != null )
        {
            this.runOnUiThread( new Runnable()
            {
                public void run()
                {
                    Bundle parameters = ConvertJSONToBundle( params );
                    if( action.equalsIgnoreCase( "feed" ) )
                    {
                        UIDialogCompletionListener listener = new UIDialogCompletionListener( action, messageHandler, successMsg, failMsg );
                        WebDialog feedDialog = new WebDialog.FeedDialogBuilder( FacebookIAPMainActivity.this, FacebookIAPMainActivity.this.CurrentSession, parameters ).setOnCompleteListener( listener ).build();
                        if( feedDialog != null )
                        {
                            feedDialog.show();
                            Log.d(TAG, "Facebook UI : Pending - " + action );
                        }
                        else
                        {
                            Log.d(TAG, "Facebook UI : Failed - " + action + " - Couldn't crate WebDialog using WebDialog.FeedDialogBuilder" );
                        }
                    }
                    else if( action.equalsIgnoreCase( "apprequests" ) )
                    {
                        UIDialogCompletionListener listener = new UIDialogCompletionListener( action, messageHandler, successMsg, failMsg );
                        WebDialog requestDialog = new WebDialog.RequestsDialogBuilder( FacebookIAPMainActivity.this, FacebookIAPMainActivity.this.CurrentSession, parameters ).setOnCompleteListener( listener ).build();
                        if( requestDialog != null )
                        {
                            requestDialog.show();
                            Log.d(TAG, "Facebook UI : Pending - " + action );
                        }
                        else
                        {
                            Log.d(TAG, "Facebook UI : Failed - " + action + " - Couldn't crate WebDialog using WebDialog.RequestsDialogBuilder" );
                        }
                    }
                    else
                    {
                        Log.d(TAG, "Facebook UI : Failed - Unknown action '" + action + "'" );
                    }
                }
            } );
            success = true;
        }
        else
        {
            Log.d(TAG, "Facebook UI : Failed - CurrentSession does not exist. Call FacebookInit and FacebookLogin before FacebookUI" );
        }

        return success;
    }

    private class UIDialogCompletionListener implements WebDialog.OnCompleteListener
    {
        private String Action;
        private String MessageHandler;
        private String SuccessMsg;
        private String FailMsg;
    
        public UIDialogCompletionListener( String action, String messageHandler, String successMsg, String failMsg )
        {
            this.Action = action;
            this.MessageHandler = messageHandler;
            this.SuccessMsg = successMsg;
            this.FailMsg = failMsg;
        }
    
        @Override
        public void onComplete(Bundle values, FacebookException error)
        {
            if( error == null )
            {
                UnityPlayer.UnitySendMessage( this.MessageHandler, this.SuccessMsg, values.toString() );
                Log.d(TAG, "Facebook UI : Success - " + this.Action + " - values = " + values );
            }
            else
            {
                UnityPlayer.UnitySendMessage( this.MessageHandler, this.FailMsg, error.getMessage() );
                Log.d(TAG, "Facebook UI : Failed - " + this.Action + " - values = " + values + " error = " + error.getMessage() );
            }
        }
    };

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
