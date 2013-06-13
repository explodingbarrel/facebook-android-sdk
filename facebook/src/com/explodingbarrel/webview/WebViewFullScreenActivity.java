package com.explodingbarrel.facebook;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import org.json.JSONObject;
import org.json.JSONArray;

import com.google.android.vending.expansion.downloader.*;

 
public class WebViewFullScreenActivity extends Activity
{
	 // Debug tag, for logging
    private static final String TAG = "WebView";
    
	private WebView FullScreenWebView;
	
	private class InternalWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        return false;
	    }
	    
	    @Override
	    public void onScaleChanged(WebView view, float oldScale, float newScale) {
	    }
	}
	
	private RelativeLayout CreateURLTab( final String url, String image, int backgroundColour, String badgeImage )
	{
		RelativeLayout tabLayout = new RelativeLayout( this );
		
		ImageButton button = new ImageButton(this);
		button.setBackgroundColor(backgroundColour);
		button.setImageResource( Helpers.getIdResource(this, image) );
		RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonLayout.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
		button.setLayoutParams(buttonLayout);
		button.setOnClickListener( new OnClickListener()
		{
				@Override
				public void onClick(View arg0)
				{
					Log.d( TAG, "FullScreenWebViewActivity Switching to '" + url + "' : Success" );
					WebViewFullScreenActivity.this.FullScreenWebView.loadUrl( url );
				}
		});
		
		tabLayout.addView( button );
		
		if( ( badgeImage != null ) && ( badgeImage.length() > 0 ) )
		{
			ImageView badge = new ImageView( this );
			badge.setImageResource( Helpers.getIdResource(this, badgeImage) );
			RelativeLayout.LayoutParams badgeLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			//badgeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			badge.setLayoutParams( badgeLayout );
			tabLayout.addView( badge );
		}
		
		return tabLayout;
	}
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		String url = getIntent().getExtras().getString("url");
		String configJson = getIntent().getExtras().getString("config");
		
		Log.d( TAG, "FullScreenWebViewActivity onCreate : url = " + url + " config = " + configJson );
		
		//Get reasonable defaults for all of the title bar settings
		String titleBarCloseImage = "";
		String titleBarCloseText = "";
		String titleBarBadgeImage = "";
		int titleBarBackgroundColour = 0xff000000;
		int titleBarTextColour = 0xffffffff;
		boolean titleBarOnTop = true;
		
		int targetWidth = 1024;
		
		JSONObject config = null;
    	try
        {
    		config = new JSONObject( configJson );
    		
    		JSONObject titleBar = config.getJSONObject( "titleBar" );
    		if( titleBar != null )
    		{
    			titleBarCloseImage = titleBar.getString( "CloseImage" );
    			titleBarBackgroundColour = titleBar.getInt( "BackgroundColour" );
    			titleBarTextColour = titleBar.getInt( "TextColour" );
    			titleBarOnTop = titleBar.getBoolean( "OnTop" );
    			titleBarBadgeImage = titleBar.getString( "BadgeImage" );
    		}
    		
    		targetWidth = config.getInt( "targetWidth" );
        }
        catch (org.json.JSONException ex)
        {
        }
        

		RelativeLayout fullFrame = new RelativeLayout( this );
        RelativeLayout.LayoutParams fullFrameLayout = new RelativeLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        LinearLayout splitFrame = new LinearLayout( this );
        splitFrame.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams splitFrameLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        splitFrame.setLayoutParams(splitFrameLayoutParams);
        
        LinearLayout titleBarFrame = new LinearLayout( this );
        titleBarFrame.setOrientation(LinearLayout.HORIZONTAL);
        titleBarFrame.setBackgroundColor(titleBarBackgroundColour);
        titleBarFrame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        ImageButton closeButton = new ImageButton(this);
        closeButton.setBackgroundColor(titleBarBackgroundColour);
        closeButton.setImageResource( Helpers.getIdResource(this, titleBarCloseImage) );
        closeButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
        closeButton.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				Log.d( TAG, "FullScreenWebViewActivity Closing : Success" );
				finish();
			}
		});
        titleBarFrame.addView(closeButton);
        
        if( config != null )
        {
	    	try
	        {
	    		JSONArray tabs = config.getJSONArray( "tabs" );
	    		if( tabs != null )
	    		{
	    			for( int i = 0; i< tabs.length(); i++ )
	    			{
	    				JSONObject tab = tabs.getJSONObject( i );
	    				
	    				final String tabUrl = tab.getString( "Url" );
	    				final String tabImage = tab.getString( "Image" );
	    				final String tabBadgeId = tab.getString( "BadgeId" );
	    				
	    				String badge = "";
	    				if( ( tabBadgeId != null ) && ( tabBadgeId.length() > 0 ) )
	    				{
	    					badge = titleBarBadgeImage;
	    				}
	    				
	    				Log.d( TAG, "FullScreenWebViewActivity Adding Tab : url = " + tabUrl + " image = " + tabImage + " badge = " + badge );
	    				
	    				RelativeLayout tabButton = CreateURLTab( tabUrl, tabImage, titleBarBackgroundColour, badge );
	    				tabButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
	    				titleBarFrame.addView( tabButton );
	    			}
	    		}
	        }
	        catch (org.json.JSONException ex)
	        {
	        }
        }
        
        this.FullScreenWebView = new WebView(this);
		this.FullScreenWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		this.FullScreenWebView.setWebViewClient(new InternalWebViewClient());
		WebSettings webSettings = this.FullScreenWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		
		//Support for diff screens
		DisplayMetrics metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
	    float scale = ((float)targetWidth) / metrics.widthPixels;
	    this.FullScreenWebView.setInitialScale( (int)( scale * 100.0f ) );
	    
		//String url = generateURL( page );
		this.FullScreenWebView.loadUrl( url );
		
		if( titleBarOnTop == true )
		{
			splitFrame.addView( titleBarFrame );
			splitFrame.addView( this.FullScreenWebView );
		}
		else
		{
			splitFrame.addView( this.FullScreenWebView );
			splitFrame.addView( titleBarFrame );
		}
		
		fullFrame.addView(splitFrame);

        setContentView(fullFrame, fullFrameLayout);
	}
 
}