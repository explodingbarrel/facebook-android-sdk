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
	
	public static boolean ExtractFromJar( String jarPath, String filePath, String outputPath )
	{
		boolean valid = false;
		Log.d(TAG, "JarExtraction ExtractFromJar jarPath = " + jarPath + " filePath = " + filePath + " outputPath = " + outputPath);
		
		JarInputStream jis = null;
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream( jarPath );
			jis = new JarInputStream( new BufferedInputStream( fis ) );
		
			JarEntry je;
			while( ( je = jis.getNextJarEntry() ) != null )
			{
				String candidate = je.getName();	
				if( candidate.equals( filePath ) == true )
				{
					Log.d(TAG, "JarExtraction Extracting " + filePath + " from " + jarPath + " to " + outputPath );
					FileOutputStream writer = new FileOutputStream( outputPath );
					
					int kBufferSize = 1024;
					byte[] buffer = new byte[kBufferSize];
			        int count;
					while( ( count = jis.read( buffer, 0, kBufferSize ) ) != -1 )
					{
						writer.write( buffer, 0, count );
					}
					
					writer.close();
					valid = true;
					break;
				}
			}
		}
		catch( Exception ex )
		{
		}
		finally
		{
			if( jis != null )
			{
				try
				{
					jis.close();
				}
				catch( Exception ex )
				{
				}
			}
		}
		
		return valid;
	}
}