package com.explodingbarrel.facebook;

import com.explodingbarrel.iap.MainActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.Intent;

import com.unity3d.player.*;
import com.facebook.*;
import com.facebook.widget.WebDialog;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import org.json.JSONObject;


public class FacebookIAPMainActivity extends com.explodingbarrel.iap.MainActivity
{
    // Debug tag, for logging
    private static final String TAG = "FacebookIAP";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult( requestCode, resultCode, data );
        Session session = Session.getActiveSession();
        if( session != null )
        {
            session.onActivityResult(this, requestCode, resultCode, data);
        }
    }
    
   

    private String AppId;
    private Session CurrentSession = null;

    boolean FacebookInit( String appId )
    {
        Log.d(TAG, "Facebook Init : appId = " + appId );

        boolean initialized = false;
        
        if( this.CurrentSession == null )
        {
            this.CurrentSession = new Session.Builder( this ).setApplicationId( appId ).build();
            if( this.CurrentSession != null )
            {
                Session.setActiveSession( this.CurrentSession );
                initialized = true;
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

        return initialized;
    }
    
    private class OpenForReadSessionStateCallback implements Session.StatusCallback
    {
        private String MessageHandler;
        private String SuccessMsg;
        private String FailMsg;
    
        public OpenForReadSessionStateCallback( String messageHandler, String successMsg, String failMsg )
        {
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
            }
        }
    };

    boolean FacebookLogin( String scope, boolean allowUI, String messageHandler, String authorizeSuccess, String authorizeFail )
    {
        Log.d( TAG, "Facebook Login : scope = " + scope + " allowUI = " + allowUI );

        boolean success = false;
        
        if( this.CurrentSession != null )
        {
            Session.OpenRequest openRequest = new Session.OpenRequest( this );
            if( openRequest != null )
            {
                List<String> permissions = Arrays.asList( scope.split( "," ) );
                openRequest.setPermissions( permissions );
                openRequest.setCallback( new OpenForReadSessionStateCallback( messageHandler, authorizeSuccess, authorizeFail ) );
                this.CurrentSession.openForRead( openRequest );
                success = true;
                Log.d(TAG, "Facebook Login : Pending" );
            }
            else
            {
                Log.d(TAG, "Facebook Login : Failed - Could not construct an OpenRequest" );
            }
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
                        FeedDialogCompletionListener listener = new FeedDialogCompletionListener( messageHandler, successMsg, failMsg );
                        WebDialog feedDialog = new WebDialog.FeedDialogBuilder( FacebookIAPMainActivity.this, CurrentSession, parameters ).setOnCompleteListener( listener ).build();
                        if( feedDialog != null )
                        {
                            feedDialog.show();
                            Log.d(TAG, "Facebook UI : Pending" );
                        }
                        else
                        {
                            Log.d(TAG, "Facebook UI : Failed - Couldn't crate WebDialog using WebDialog.FeedDialogBuilder" );
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

    private class FeedDialogCompletionListener implements WebDialog.OnCompleteListener
    {
        private String MessageHandler;
        private String SuccessMsg;
        private String FailMsg;
    
        public FeedDialogCompletionListener( String messageHandler, String successMsg, String failMsg )
        {
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
                Log.d(TAG, "Facebook UI : Success - values = " + values );
            }
            else
            {
                UnityPlayer.UnitySendMessage( this.MessageHandler, this.FailMsg, error.getMessage() );
                Log.d(TAG, "Facebook UI : Failed - values = " + values + " error = " + error.getMessage() );
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
