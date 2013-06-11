package com.explodingbarrel.facebook;

import android.app.Dialog;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

public class WebViewDialog extends Dialog
{
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
	        return false;
	    }
	    
	    @Override
	    public void onScaleChanged( WebView view, float oldScale, float newScale ) 
	    {
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
        RelativeLayout.LayoutParams dialogFrameLayout = new RelativeLayout.LayoutParams( this.Width, this.Height );
        dialogFrameLayout.leftMargin = this.X;
        dialogFrameLayout.topMargin = this.Y;

        WebView webView = new WebView(this.Parent);
        webView.setLayoutParams( new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) );
        webView.setWebViewClient( new DialogWebViewClient() );
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled( true );
		webSettings.setLoadWithOverviewMode( true );
		webSettings.setUseWideViewPort(true);
		
		float scale = ((float)this.TargetWidth) / this.Width;
		webView.setInitialScale( (int)( scale * 100.0f ) );
	    
		webView.loadUrl( this.Url );

		dialogFrame.addView( webView );
        	
		setContentView( dialogFrame, dialogFrameLayout );
	}
}