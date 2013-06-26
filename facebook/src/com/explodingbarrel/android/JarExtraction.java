package com.explodingbarrel.android;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;

class JarExtraction 
{
	// Debug tag, for logging
    private static final String TAG = "JarExtraction";
    
    String JarPath = null;
    String FilePath = null;
    String OutputPath = null;
    
    FileInputStream FIS = null;
    JarInputStream JIS = null;
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
	    	this.FIS = new FileInputStream( this.JarPath );
			this.JIS = new JarInputStream( new BufferedInputStream( this.FIS ) );
			
			//Find the entry we are trying to extract
			JarEntry je;
			while( ( je = this.JIS.getNextJarEntry() ) != null )
			{
				String candidate = je.getName();	
				if( candidate.equals( this.FilePath ) == true )
				{
					Log.d(TAG, "JarExtraction Extracting " + this.FilePath + " from " + this.JarPath + " to " + this.OutputPath );
					this.Writer = new FileOutputStream( this.OutputPath );
					this.Buffer = new byte[kBufferSize];
					this.StepCount = 0;
					this.BytesWritten = 0;
					success = true;
					break;
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