package com.explodingbarrel.facebook;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.DisplayMetrics;
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
	private ImageButton CloseButton;
	
	private class InternalWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        return false;
	    }
	    
	    @Override
	    public void onScaleChanged(WebView view, float oldScale, float newScale) {
	    }
	}
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		String url = getIntent().getExtras().getString("url");
		String tabsJson = getIntent().getExtras().getString("tabs");
		
		Log.d( TAG, "FullScreenWebViewActivity onCreate : url = " + url + " tabs = " + tabsJson );
		
		RelativeLayout fullFrame = new RelativeLayout( this );
        RelativeLayout.LayoutParams fullFrameLayout = new RelativeLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        
        LinearLayout splitFrame = new LinearLayout( this );
        splitFrame.setOrientation(LinearLayout.VERTICAL);
        splitFrame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        LinearLayout buttonFrame = new LinearLayout( this );
        buttonFrame.setOrientation(LinearLayout.HORIZONTAL);
        buttonFrame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        this.CloseButton = new ImageButton(this);
    	if( this.CloseButton != null )
    	{
    		this.CloseButton.setImageResource( Helpers.getIdResource(this, "drawable/back") );
    		this.CloseButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    		this.CloseButton.setOnClickListener( new OnClickListener()
    												{
    			 										@Override
    			 										public void onClick(View arg0)
    			 										{
    			 											Log.d( TAG, "FullScreenWebViewActivity Closing : Success" );
    			 											finish();
    			 										}
    												});
    		buttonFrame.addView(this.CloseButton);
    	}
    	
    	try
        {
    		JSONArray tabs = new JSONArray( tabsJson );
    		
    		if( tabs != null )
    		{
    			for( int i = 0; i< tabs.length(); i++ )
    			{
    				JSONObject tab = tabs.getJSONObject( i );
    				
    				final String tabText = tab.getString( "Text" );
    				final String tabUrl = tab.getString( "Url" );
    				final String tabImage = tab.getString( "Image" );

    				Button tabButton = new Button(this);
    				tabButton.setText( tabText );
    				if( tabImage.length() > 0 )
    				{
    					tabButton.setCompoundDrawablesWithIntrinsicBounds(Helpers.getIdResource(this, tabImage), 0, 0, 0 );
    				}
    				tabButton.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    				tabButton.setOnClickListener( new OnClickListener()
					{
							@Override
							public void onClick(View arg0)
							{
								Log.d( TAG, "FullScreenWebViewActivity Switching to '" + tabUrl + "' : Success" );
								WebViewFullScreenActivity.this.FullScreenWebView.loadUrl( tabUrl );
							}
					});
    				buttonFrame.addView( tabButton );
    			}
    		}
        }
        catch (org.json.JSONException ex)
        {
        }

		this.FullScreenWebView = new WebView(this);
		this.FullScreenWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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
	    
		//String url = generateURL( page );
		this.FullScreenWebView.loadUrl( url );
		
		splitFrame.addView( buttonFrame );
		splitFrame.addView( this.FullScreenWebView );
		
		fullFrame.addView(splitFrame);

        setContentView(fullFrame, fullFrameLayout);
	}
 
}