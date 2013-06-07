package com.explodingbarrel.facebook;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.*;
import android.widget.Button;

import com.google.android.vending.expansion.downloader.*;
 
public class FullScreenWebViewActivity extends Activity
{
	 // Debug tag, for logging
    private static final String TAG = "WebView";
    
    private boolean Debug = false;
    private String BaseUrl;
    private String SToken;
    
	private WebView FullScreenWebView;
	private Button CloseButton;
	private Button InfoButton;
	private Button LeaderboardButton;
	
	private class InternalWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        return false;
	    }
	    
	    @Override
	    public void onScaleChanged(WebView view, float oldScale, float newScale) {
	    }
	}
	
	private String generateURL( int page )
	{
		String url = "";
		
		url = this.BaseUrl;
		if( this.Debug == false )
		{
			url += "/web?";
			url += "page=" + page;
			url += "&";
			url += "stoken=" + this.SToken;
		}
		
		return url;
	}
 
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		this.BaseUrl = getIntent().getExtras().getString("baseurl");
		this.SToken = getIntent().getExtras().getString("stoken");
		int page = getIntent().getExtras().getInt("page");
		this.Debug = getIntent().getExtras().getBoolean("debug");
		
		Log.d( TAG, "FullScreenWebViewActivity onCreate : baseurl = " + this.BaseUrl + " stoken = " + this.SToken + " page = " + page + " debug = " + this.Debug );
    	
    	setContentView( Helpers.getLayoutResource(this, "webviewfullscreen") );
    	
    	this.CloseButton = (Button)findViewById( Helpers.getIdResource(this, "close") );
    	if( this.CloseButton != null )
    	{
    		this.CloseButton.setOnClickListener( new OnClickListener()
    												{
    			 										@Override
    			 										public void onClick(View arg0)
    			 										{
    			 											Log.d( TAG, "FullScreenWebViewActivity Closing : Success" );
    			 											finish();
    			 										}
    												});
    	}
    	this.InfoButton = (Button)findViewById( Helpers.getIdResource(this, "info") );
    	if( this.InfoButton != null )
    	{
    		this.InfoButton.setOnClickListener( new OnClickListener()
    												{
    			 										@Override
    			 										public void onClick(View arg0)
    			 										{
    			 											String url = generateURL( 0 );
    			 											Log.d( TAG, "FullScreenWebViewActivity Switching to Tournament info (" + url + ") : Success" );
    			 											FullScreenWebViewActivity.this.FullScreenWebView.loadUrl( url );
    			 										}
    												});
    	}
    	this.LeaderboardButton = (Button)findViewById( Helpers.getIdResource(this, "leaderboard") );
    	if( this.LeaderboardButton != null )
    	{
    		this.LeaderboardButton.setOnClickListener( new OnClickListener()
    												{
    			 										@Override
    			 										public void onClick(View arg0)
    			 										{
    			 											String url = generateURL( 1 );
    			 											Log.d( TAG, "FullScreenWebViewActivity Switching to Leaderboard info (" + url + ") : Success" );
    			 											FullScreenWebViewActivity.this.FullScreenWebView.loadUrl( url );
    			 										}
    												});
    	}
    	
    	

    	View view = findViewById( Helpers.getIdResource(this, "webview") );
    	if( view != null )
    	{
    		this.FullScreenWebView = (WebView)view;
    		this.FullScreenWebView.setWebViewClient(new InternalWebViewClient());
    		WebSettings webSettings = this.FullScreenWebView.getSettings();
    		webSettings.setJavaScriptEnabled(true);
    		webSettings.setLoadWithOverviewMode(true);
    		webSettings.setUseWideViewPort(true);
    		
    		//Support for diff screens
    	    DisplayMetrics metrics = new DisplayMetrics();
    	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	    float scale = 1024.0f / metrics.widthPixels;
    	    this.FullScreenWebView.setInitialScale( (int)( scale * 100.0f ) );
    	    
    		String url = generateURL( page );
    		this.FullScreenWebView.loadUrl( url );
    	}
    	else
    	{
    		Log.d(TAG, "WebViewShowFullscreen : Failed - Couldn't load the fullscreenwebview from the layout webviewfullscreen" );
    	}
	}
 
}