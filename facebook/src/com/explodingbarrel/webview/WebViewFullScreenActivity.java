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

import java.util.List;
import java.util.ArrayList;

import com.unity3d.player.*;

import com.google.android.vending.expansion.downloader.*;

 
public class WebViewFullScreenActivity extends Activity
{
	 // Debug tag, for logging
    private static final String TAG = "WebView";
    
	private WebView FullScreenWebView;
	
	class TabView
	{
		public String Url = null;
		public String Image = null;
		public String ImageSelected = null;
		public ImageButton TabImage = null;
		public int BadgeId = -1;
		public RelativeLayout BadgeLayout = null;
		public TextView BadgeText = null;
		
		public TabView( String url, String image, String imageSelected, ImageButton tabImage, int badgeId, RelativeLayout badgeLayout, TextView badgeText )
		{
			this.Url = url;
			this.Image = image;
			this.ImageSelected = imageSelected;
			this.TabImage = tabImage;
			this.BadgeId = badgeId;
			this.BadgeLayout = badgeLayout;
			this.BadgeText = badgeText;
		}
	}
	
	private List<TabView> Tabs = new ArrayList<TabView>();
	
	
	private class InternalWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        return false;
	    }
	    
	    @Override
	    public void onScaleChanged(WebView view, float oldScale, float newScale) {
	    }
	}
	
	public class BadgeJavaScriptInterface
	{
		WebViewFullScreenActivity WebView;

		BadgeJavaScriptInterface(WebViewFullScreenActivity webView)
		{
	        this.WebView = webView;
	    }

	    public void setBadge(int id, String value)
	    {
	    	this.WebView.updateBadge( id, value );
	    }
	}
	
	public class WebViewScriptInterface
	{
		WebViewFullScreenActivity WebView;

		WebViewScriptInterface(WebViewFullScreenActivity webView)
		{
	        this.WebView = webView;
	    }

	    public void close()
	    {
	    	this.WebView.finish();
	    }
	}
	
	private void updateBadge( int id, final String value )
	{
		Log.d( TAG, "FullScreenWebViewActivity updateBadge : id = " + id + " value = " + value );
		for( final TabView tab: this.Tabs) 
		{
			if( tab.BadgeId == id )
			{
				tab.BadgeLayout.post( new Runnable()
	    		{
	    			public void run()
	    			{
	    				boolean visible = false;   				
	    				if( ( value != null ) && ( value.length() > 0 ) )
	    				{
		    				try
		    				{
		    				   int val = Integer.parseInt( value );
		    				   if( val != 0 )
		    				   {
		    					   visible = true;
		    				   }
		    				}
		    				catch (NumberFormatException nfe)
		    				{
		    				}
	    				}

	    	    		if( visible == true  )
	    	    		{
	    	    			tab.BadgeText.setText( value );
	    	    			tab.BadgeLayout.setVisibility(View.VISIBLE);
	    	    		}
	    	    		else
	    	    		{
	    	    			tab.BadgeText.setText( "" );
	    	    			tab.BadgeLayout.setVisibility(View.INVISIBLE);
	    	    		}
	    			}
	    		});
				break;
			}
		}
	}
	
	private void loadPage( String url )
	{
		Log.d( TAG, "FullScreenWebViewActivity Switching to '" + url + "' : Success" );
		this.FullScreenWebView.loadUrl( url );
		
		//Update the correct selected items
		for( TabView tab: this.Tabs) 
		{
			String resourceName = tab.Image;
			boolean hasSelected = ( tab.ImageSelected != null ) && ( tab.ImageSelected.length() > 0 );
			if( ( tab.Url.equals( url ) == true ) && ( hasSelected == true ) )
			{
				resourceName = tab.ImageSelected;
			}
			
			Log.d( TAG, "FullScreenWebViewActivity setting '" + tab.Url + "' to " + resourceName );
			tab.TabImage.setImageResource( Helpers.getIdResource(this, resourceName) );
		}
	}
	
	private RelativeLayout CreateURLTab( final String url, String image, String imageSelected, int backgroundColour, int badgeId, String badgeImage, int badgeLeftMargin, int badgeTopMargin )
	{
		RelativeLayout tabLayout = new RelativeLayout( this );
		
		ImageButton button = new ImageButton(this);
		button.setBackgroundColor(backgroundColour);
		button.setImageResource( Helpers.getIdResource(this, image) );
		RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		buttonLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
		button.setLayoutParams(buttonLayout);
		button.setOnClickListener( new OnClickListener()
		{
				@Override
				public void onClick(View arg0)
				{
					Log.d( TAG, "FullScreenWebViewActivity Switching to '" + url + "' : Success" );
					WebViewFullScreenActivity.this.loadPage( url );
				}
		});
		
		tabLayout.addView( button );
		
		RelativeLayout badgeFrame = null;
		TextView badgeTextView = null;
		
		if( ( badgeImage != null ) && ( badgeImage.length() > 0 ) )
		{
			badgeFrame = new RelativeLayout( this );
			RelativeLayout.LayoutParams badgeFrameLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			if( badgeLeftMargin < 0 )
			{
				badgeLeftMargin = 0;
			}
			if( badgeTopMargin < 0 )
			{
				badgeTopMargin = 0;
			}
			badgeFrameLayout.leftMargin = badgeLeftMargin;
			badgeFrameLayout.topMargin = badgeTopMargin;
			badgeFrame.setLayoutParams( badgeFrameLayout );
			
			RelativeLayout badge = new RelativeLayout( this );
				RelativeLayout.LayoutParams badgeLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				badge.setLayoutParams( badgeLayout );
				
					ImageView badgeImageView = new ImageView( this );
					badgeImageView.setImageResource( Helpers.getIdResource(this, badgeImage) );
					RelativeLayout.LayoutParams badgeImageLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					badgeImageLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
					badgeImageView.setLayoutParams( badgeImageLayout );
					badge.addView( badgeImageView );
					
					badgeTextView = new TextView( this );
					badgeTextView.setText( "0" );
					badgeTextView.setTextColor( 0xffffffff );
					RelativeLayout.LayoutParams badgeTextLayout = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					badgeTextLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
					badgeTextView.setLayoutParams( badgeTextLayout );
					badge.addView( badgeTextView );
				
				badgeFrame.addView( badge );
				
			tabLayout.addView( badgeFrame );
		}
		
		this.Tabs.add( new TabView( url, image, imageSelected, button, badgeId, badgeFrame, badgeTextView ) );
		
		return tabLayout;
	}
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		DisplayMetrics metrics = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		String url = getIntent().getExtras().getString("url");
		String configJson = getIntent().getExtras().getString("config");
		
		Log.d( TAG, "FullScreenWebViewActivity onCreate : url = " + url + " config = " + configJson );
		
		//Get reasonable defaults for all of the title bar settings
		String titleBarCloseImage = "";
		String titleBarCloseText = "";
		String titleBarBadgeImage = "";
		String titleBarHighlightedPostFix = "";
		String titleBarHiResPostFix = "";
		int titleBarBackgroundColour = 0xff000000;
		int titleBarTextColour = 0xffffffff;
		boolean titleBarOnTop = true;
		boolean titleBarExpandToFit = true;
		
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
    			titleBarExpandToFit = titleBar.getBoolean( "ExpandToFit" );
    			titleBarHighlightedPostFix = titleBar.getString( "HighlightedImagePostFix" );
    			titleBarHiResPostFix = titleBar.getString( "HiResImagePostFix" );
    		}
    		
    		targetWidth = config.getInt( "targetWidth" );
        }
        catch (org.json.JSONException ex)
        {
        	Log.d( TAG, "FullScreenWebViewActivity JSON Error in config : " + ex.getMessage() );
        }
        
        float weight = 0.0f;
        if( titleBarExpandToFit == true )
        {
        	weight = 1.0f;
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
        closeButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, weight));
        closeButton.setOnClickListener( new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				Log.d( TAG, "FullScreenWebViewActivity Closing : Success" );
				UnityPlayer.UnitySendMessage( "WebViewCallbacks", "WebViewWillHide", "" );
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
	    			Log.d( TAG, "FullScreenWebViewActivity Total Tabs : " + tabs.length() );
	    			for( int i = 0; i< tabs.length(); i++ )
	    			{
	    				JSONObject tab = tabs.getJSONObject( i );
	    				
	    				String tabUrl = tab.getString( "Url" );
	    				String tabImage = tab.getString( "Image" );
	    				String tabImageSelected = null;
	    				if( ( titleBarHighlightedPostFix != null ) && ( titleBarHighlightedPostFix.length() > 0 ) )
	    				{
	    					tabImageSelected = tabImage + titleBarHighlightedPostFix;
	    				}
	    				
	    				int tabBadgeId = -1;
	    				int tabBadgeLeftMargin = 0;
	    				int tabBadgeTopMargin = 0;

	    				JSONObject badge = tab.getJSONObject( "Badge" );
	    				if( badge != null )
	    				{
	    					tabBadgeId = badge.getInt( "Id" );
	    					tabBadgeLeftMargin = badge.getInt( "LeftMargin" );
	    					tabBadgeTopMargin = badge.getInt( "TopMargin" );
	    				}
	    				
	    				String badgeImage = "";
	    				if( tabBadgeId != -1 )
	    				{
	    					badgeImage = titleBarBadgeImage;
	    				}
	    				
	    				//Convert from dpi to pixels
    					int tabBadgeLeftMarginPx = (int) (tabBadgeLeftMargin * metrics.density + 0.5f);
    					int tabBadgeTopMarginPx = (int) (tabBadgeTopMargin * metrics.density + 0.5f);
    					
    					Log.d( TAG, "FullScreenWebViewActivity Adding Tab : url = " + tabUrl + " image = " + tabImage + " tabImageSelected = " + tabImageSelected + " tabBadgeId = " + tabBadgeId + " badge = " + badgeImage + " offset = (" + tabBadgeLeftMarginPx + "," + tabBadgeTopMarginPx + ")");
	    				
	    				RelativeLayout tabButton = CreateURLTab( tabUrl,
	    															tabImage,
	    															tabImageSelected,
	    															titleBarBackgroundColour,
	    															tabBadgeId, 
	    															badgeImage,
	    															tabBadgeLeftMarginPx,
	    															tabBadgeTopMarginPx );
	    				tabButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, weight));
	    				titleBarFrame.addView( tabButton );
	    				if( tabBadgeId != -1 )
	    				{
	    					updateBadge( tabBadgeId, "0" );
	    				}
	    				 
	    			}
	    		}
	        }
	        catch (org.json.JSONException ex)
	        {
	        	Log.d( TAG, "FullScreenWebViewActivity JSON Error in Tabs : " + ex.getMessage() );
	        }
        }
        
        this.FullScreenWebView = new WebView(this);
		this.FullScreenWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		this.FullScreenWebView.setWebViewClient(new InternalWebViewClient());
		WebSettings webSettings = this.FullScreenWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		
		this.FullScreenWebView.addJavascriptInterface(new BadgeJavaScriptInterface(this), "Badges");
		this.FullScreenWebView.addJavascriptInterface(new WebViewScriptInterface(this), "WebView");
		
		//Support for diff screens
	    float scale = ((float)targetWidth) / metrics.widthPixels;
	    this.FullScreenWebView.setInitialScale( (int)( scale * 100.0f ) );
	    
	    loadPage( url );
		
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
        
        UnityPlayer.UnitySendMessage("WebViewCallbacks", "WebViewDidShow", "");
	}
 
}