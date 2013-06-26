package com.explodingbarrel.android;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

class JarExtraction 
{
	// Debug tag, for logging
    private static final String TAG = "JarExtraction";
    
    String JarPath = null;
    String FilePath = null;
    String OutputPath = null;
    
    InputStream JIS = null;
    FileOutputStream Writer = null;
    
    int kBufferSize = 1024;
    byte[] Buffer = null;
    
    int StepCount = 0;
    int BytesWritten = 0;
	
    public JarExtraction( String jarPath, String filePath, String outputPath )
    {
    	this.JarPath = jarPath;
    	this.FilePath = filePath;
    	this.OutputPath = outputPath;
    	
    	Log.d(TAG, "JarExtraction jarPath = " + this.JarPath + " filePath = " + this.FilePath + " outputPath = " + this.OutputPath);
    }
    
    public boolean StartExtractFromJar()
    {
    	boolean success = false;
    	try
    	{
    		JarFile jarFile = new JarFile( this.JarPath );
    		if( jarFile != null )
    		{
    			JarEntry je = jarFile.getJarEntry( this.FilePath );
    			if( je != null )
    			{
    				Log.d(TAG, "JarExtraction Extracting " + this.FilePath + " from " + this.JarPath + " to " + this.OutputPath );
    				this.JIS = jarFile.getInputStream( je );
					this.Writer = new FileOutputStream( this.OutputPath );
					this.Buffer = new byte[kBufferSize];
					this.StepCount = 0;
					this.BytesWritten = 0;
					success = true;
    			}
    		}
    	}
    	catch( Exception ex )
    	{
    	}
    	
    	return success;
    }
    
    public boolean StepExtractFromJar( int maxBytesToRead )
    {
    	boolean complete = false;
    	
    	if( this.StepCount % 1000 == 0 )
    	{
    		Log.d(TAG, "JarExtraction StepExtractFromJar Steps:" + this.StepCount + " Bytes Written:" + this.BytesWritten );
    	}
    	this.StepCount++;

    	try
    	{
    		while( ( maxBytesToRead > 0 ) && ( complete == false ) )
    		{
    			int bytesRead = this.JIS.read( this.Buffer, 0, kBufferSize );
    	    	if( bytesRead != -1 )
    	    	{
    	    		maxBytesToRead -= bytesRead;
    	    		this.Writer.write( this.Buffer, 0, bytesRead );
    	    		this.BytesWritten += bytesRead;
    	    	}
    	    	else
    	    	{
    	    		complete = true;
    	    	}
    		}
    	}
    	catch( Exception ex )
    	{
    		complete = true;
    	}
    	
    	return complete;
    }
    
    public void CompleteExtractFromJar()
    {
    	try
    	{
    		Log.d(TAG, "JarExtraction ExtractionComplete " + this.OutputPath + " Bytes Written:" + this.BytesWritten );
    		this.Writer.close();
    		this.JIS.close();
    	}
    	catch( Exception ex )
    	{
    	}
    }
    
	public boolean SyncExtractFromJar()
	{
    	boolean success = this.StartExtractFromJar();
    	if( success == true )
    	{
	    	boolean complete = false;
	    	while( ( complete = this.StepExtractFromJar(kBufferSize) ) == false )
	    	{
	    	}
	    	this.CompleteExtractFromJar();
    	}
    	return success;
	}
		
}