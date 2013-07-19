package com.explodingbarrel.facebook;

import android.app.Dialog;
import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.Gravity;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import com.unity3d.player.*;

public class WebViewDialog extends Dialog
{
	 // Debug tag, for logging
    private static final String TAG = "WebView";
	
	private Activity Parent = null;
	private String Url = null;
	private int TargetWidth = 1024;
	private int X = 0;
	private int Y = 0;
	private int Width = 0;
	private int Height = 0;
	
	private class DialogWebViewClient extends WebViewClient
	{
	    @Override
	    public boolean shouldOverrideUrlLoading( WebView view, String url )
	    {
	    	Log.d( TAG, "DialogWebViewClient shouldOverrideUrlLoading : url = " + url );
	    	
	    	if( url.startsWith( "client://" ) == true )
	    	{	
	    		UnityPlayer.UnitySendMessage( "webview_callbacks", "OnUrl", url );
	    		return true;
	    	}
	    	else
	    	{
	    		return false;
	    	}
	    }
	    
	    @Override
	    public void onScaleChanged( WebView view, float oldScale, float newScale ) 
	    {
	    }
	    
	    @Override
	    public void onPageFinished(WebView view, String url)
	    {
	    	view.setBackgroundColor( 0x00000000 );
	    }

	}
	
	public WebViewDialog( Activity a, String url, int targetWidth, int x, int y, int width, int height )
	{
		super(a);
		this.Parent = a;
		this.Url = url;
		this.TargetWidth = targetWidth;
		this.X = x;
		this.Y = y;
		this.Width = width;
		this.Height = height;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		RelativeLayout dialogFrame = new RelativeLayout( this.Parent );
		dialogFrame.setBackgroundColor( 0xff000000 );

        WebView webView = new WebView(this.Parent);
        webView.setWebViewClient( new DialogWebViewClient() );
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled( true );
		webSettings.setLoadWithOverviewMode( true );
		webSettings.setUseWideViewPort(true);
		webView.setBackgroundColor( 0x00000000 );
		
		float scale = ((float)this.TargetWidth) / this.Width;
		webView.setInitialScale( (int)( scale * 100.0f ) );
	    
		webView.loadUrl( this.Url );
		
		RelativeLayout.LayoutParams webViewLayout = new RelativeLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );
		dialogFrame.addView( webView, webViewLayout );
		
		RelativeLayout.LayoutParams dialogLayout = new RelativeLayout.LayoutParams( this.Width, this.Height );
		setContentView( dialogFrame, dialogLayout );
	}
}