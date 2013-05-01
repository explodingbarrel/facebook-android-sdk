package com.explodingbarrel.facebook;

import com.explodingbarrel.iap.MainActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.Intent;

import com.unity3d.player.*;
import com.facebook.*;

import java.util.Arrays;
import java.util.List;


public class FacebookIAPMainActivity extends com.explodingbarrel.iap.MainActivity
{
    // Debug tag, for logging
    private static final String TAG = "FacebookIAP";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult( requestCode, resultCode, data );
    }
    
    private Session.StatusCallback SessionStateCallback = new Session.StatusCallback()
    {
        @Override
        public void call(Session session, SessionState state, Exception exception)
        {
            onSessionStateChange( session, state, exception );
        }
    };

    private String AppId;
    private Session CurrentSession = null;

    private String MessageHandlerName;
    private String AuthorizeSuccessMessage;
    private String AuthorizeFailedMessage;

    boolean Init( String appId, String messageHandler, String authorizeSuccess, String authorizeFail )
    {
        Log.d(TAG, "Facebook Init : appId = " + appId );

        boolean initialized = false;
        
        if( this.CurrentSession == null )
        {
            this.MessageHandlerName = messageHandler;
            this.AuthorizeSuccessMessage = authorizeSuccess;
            this.AuthorizeFailedMessage = authorizeFail;

            this.CurrentSession = new Session.Builder( this ).setApplicationId( appId ).build();
            if( this.CurrentSession != null )
            {
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
    
    boolean Login( String scope, boolean allowUI )
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
                openRequest.setCallback( this.SessionStateCallback );
                this.CurrentSession.openForRead( openRequest );
                success = true;
                Log.d(TAG, "Facebook Login : Success" );
            }
            else
            {
                Log.d(TAG, "Facebook Login : Failed - Could not construct an OpenRequest" );
            }
        }
        else
        {
            Log.d(TAG, "Facebook Login : Failed - CurrentSession does not exist. Call Init before Login" );
        }

        return success;
    }

    void Logout()
    {
        if( this.CurrentSession != null )
        {
            this.CurrentSession.close();
        }
    }

    void onSessionStateChange(Session session, SessionState state, Exception exception)
    {
        if( session == this.CurrentSession )
        {
            Log.d(TAG, "Facebook onSessionStateChange : state = " + state );
            switch( state )
            {
                case OPENED:
                {
                    String accessToken = session.getAccessToken();
                    UnityPlayer.UnitySendMessage( this.MessageHandlerName, this.AuthorizeSuccessMessage, accessToken );
                    Log.d(TAG, "Facebook OPENED : accessToken = " + accessToken );
                    break;
                }
                case CLOSED:
                case CLOSED_LOGIN_FAILED:
                {
                    UnityPlayer.UnitySendMessage( this.MessageHandlerName, this.AuthorizeFailedMessage, "0" );
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
    }
 }
