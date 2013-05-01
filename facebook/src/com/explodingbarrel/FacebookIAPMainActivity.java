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
    
    boolean Init( String appId, String messageHandler, String authorizeSuccess, String authorizeFail )
    {
        boolean initialized = false;
        
        if( this.CurrentSession == null )
        {
            this.CurrentSession = new Session.Builder( this ).setApplicationId( appId ).build();
            if( this.CurrentSession != null )
            {
                initialized = true;
            }
        }

        return initialized;
    }
    
    boolean Login( String scope, boolean allowUI )
    {
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
            }
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
        if( session == CurrentSession )
        {
            switch( state )
            {
                case OPENED:
                {
                    break;
                }
                case CLOSED:
                case CLOSED_LOGIN_FAILED:
                {
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
