package com.explodingbarrel.android;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;

import com.unity3d.player.*;

class APKVerify 
{
	// Debug tag, for logging
    private static final String TAG = "APKVerify";
	
    String JarPath = null;
    String BundleID = null;
    String Salt = null;
    
    public enum VerifyStage
    {
    	Idle,
    	GeneratingFileList,
    	BuildingDigest,
    	Complete,
    	Error
    };
    
    VerifyStage Stage = VerifyStage.Idle;
    
    JarFile JF = null;
    
    MessageDigest MD = null;
    Mac MAC = null;
    
    Enumeration<JarEntry> Entries = null;
    List<JarEntry> EntriesForProcessing = null;
    
    Iterator<JarEntry> ProcessingIt = null;
    InputStream JIS = null;
    
    int kBufferSize = 1024;
    byte[] Buffer = null;
    
    int StepCount = 0;
    int FilesExamined = 0;
    int BytesProcessed = 0;
    
    public APKVerify( String jarPath )
    {
    	this.JarPath = jarPath;
    	
    	//Log.d(TAG, "APKVerify jarPath = " + this.JarPath );
    }
    
    public APKVerify( String jarPath, String bundleID, String salt )
    {
    	this.JarPath = jarPath;
    	this.BundleID = bundleID;
    	this.Salt = salt;
    	
    	//Log.d(TAG, "APKVerify jarPath = " + this.JarPath + " bundleID = " + bundleID + " salt = " + salt );
    }
    
    public boolean StartSha1Signature()
    {
    	boolean success = false;
    	
		switch( this.Stage )
		{
    		case Idle:
    		{
    			try
    	    	{
	    			this.JF = new JarFile( this.JarPath );
	        		if( this.JF != null )
	        		{
	        			//Log.d(TAG, "APKVerify StartSha1Verify" );
	        			
	        			if( this.Salt == null )
	        			{
	        				this.MD = MessageDigest.getInstance("SHA-1");
	        				this.MAC = null;
	        			}
	        			else
	        			{
	        				this.MAC = Mac.getInstance("HmacSHA1");
	        				this.MD = null;
	        				
	        				String activityKeyHash = "";
	        				try 
	        			    {
	        					Activity activity = UnityPlayer.currentActivity;
	        					if( activity != null )
	        					{
	    	    			        PackageInfo info = activity.getPackageManager().getPackageInfo(this.BundleID, PackageManager.GET_SIGNATURES);
	    	    			        for (Signature signature : info.signatures) 
	    	    			        {
	    	    			        	MessageDigest md = MessageDigest.getInstance("SHA");
	    	    			        	md.update(signature.toByteArray());
	    	    			        	activityKeyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
	    	    			        }
	        					}
	        			    } 
	        			    catch (Exception e) {}
	        				
	        				String key = this.Salt + "." + activityKeyHash;
	        				SecretKeySpec secret = new SecretKeySpec( key.getBytes("UTF-8"), this.MAC.getAlgorithm() );
	        				this.MAC.init(secret);
	        			}
	        			
	        			this.Stage = VerifyStage.GeneratingFileList;

	        			this.Entries = this.JF.entries();
	        			this.EntriesForProcessing = new ArrayList<JarEntry>();
	        			this.ProcessingIt = null;
	        		    this.JIS = null;
	        		    this.Buffer = new byte[kBufferSize];       		
	        		    this.StepCount = 0;
	        		    this.FilesExamined = 0;
	        		    this.BytesProcessed = 0;
	        				
	        			success = true;
	        		}
    	    	}
    			catch( Exception ex )
    	    	{
    				//Log.d(TAG, "APKVerify Error - Retrieving entries from JAR " + ex.getMessage() );
    				this.Stage = VerifyStage.Error;
    	    	}
    			
    			break;
    		}
    		default:
    		{
    			break;
    		}
		}
    	
    	return success;
    }
    
    private static class JarEntryComparator implements Comparator<JarEntry> {
        @Override
        public int compare(JarEntry lhs, JarEntry rhs)
        {
        	String lhsName = lhs.getName();
        	String rhsName = rhs.getName();
        	//Log.d(TAG, "Compare " + lhsName + " " + rhsName );
        	return String.CASE_INSENSITIVE_ORDER.compare(lhsName, rhsName);
        }
    }
    
    private void StepGeneratingFileList( int maxFilesToExamine )
    {
    	boolean complete = false;
    	
    	while( ( complete == false ) && ( maxFilesToExamine > 0 ) )
    	{
    		if( this.Entries.hasMoreElements() == true )
    		{
    			this.FilesExamined++;
    			maxFilesToExamine--;
    			
    			try
    			{
	    			final JarEntry je = this.Entries.nextElement();
	    			String name = je.getName();
	    			if( name != null )
	    			{	
	    				if( ( name.startsWith( "assets/bin/Data/Managed/" ) == true ) && ( name.endsWith( ".dll" ) == true ) )
	    				{
	    					this.EntriesForProcessing.add( je );
	    				}
	    			}
    			}
    			catch( Exception ex )
	        	{
    				//Log.d(TAG, "APKVerify Error - Looking for .dlls " + ex.getMessage() );
    				this.Stage = VerifyStage.Error;
        			complete = true;
	        	}
    		}
    		else
    		{   			
    			Collections.sort(this.EntriesForProcessing, new JarEntryComparator());
    			
//    			//Log.d(TAG, "APKVerify List of Files for Hashing (" + this.EntriesForProcessing.size() + ")");
//    			for( JarEntry je : this.EntriesForProcessing)
//    			{
//    				String name = je.getName();
//    				Log.d(TAG, "    " + name );
//    			}
    			
    			this.ProcessingIt = this.EntriesForProcessing.iterator();
    			this.Stage = VerifyStage.BuildingDigest;
    			complete = true;
    		}
    	}
    }
    
    private void StepBuildingDigest( int maxBytesToRead )
    {
    	boolean complete = false;
    	while( ( complete == false ) && ( maxBytesToRead > 0 ) )
    	{
	    	//If we don't have active input stream then we need to find the next valid file
	    	if( this.JIS == null )
	    	{
	    		if( this.ProcessingIt.hasNext() == true )
	    		{
	    			final JarEntry je = this.ProcessingIt.next();
	    			try
	    			{
	    				this.JIS = this.JF.getInputStream( je );
	    			}
	    			catch( Exception ex )
	    			{
	    				//Log.d(TAG, "APKVerify Error - Couldn't Open InputStream " + ex.getMessage() );
		        		this.Stage = VerifyStage.Error;
		        		complete = true;
	    			}
	    		}
	    		else
	    		{
	    			this.Stage = VerifyStage.Complete;
	    			complete = true;
	    		}	
	    	}
	    	else
	    	{
	    		try
	        	{
        			int bytesRead = this.JIS.read( this.Buffer, 0, kBufferSize );
        	    	if( bytesRead != -1 )
        	    	{
        	    		maxBytesToRead -= bytesRead;
        	    		this.updateDigest( this.Buffer, 0, bytesRead );
        	    		this.BytesProcessed += bytesRead;
        	    	}
        	    	else
        	    	{
        	    		this.JIS.close();
        	    		this.JIS = null;
        	    		complete = true;
        	    	}
	        	}
	        	catch( Exception ex )
	        	{
	        		//Log.d(TAG, "APKVerify Error - Reading data from file " + ex.getMessage() );
	        		this.Stage = VerifyStage.Error;
	        		complete = true;
	        	}
	    	}
	    }
    	
    	
    }
    
    public boolean StepSha1Signature( int maxBytesToRead, int maxFilesToExamine, int stepsBeforeLog )
    {
    	boolean complete = false;
    	
//    	if( this.StepCount % stepsBeforeLog == 0 )
//    	{
//    		Log.d(TAG, "APKVerify StepExtractFromJar Steps:" + this.StepCount + " Files Examined: " + this.FilesExamined + " Bytes Processed: " + this.BytesProcessed );
//    	}
//    	this.StepCount++;
    	
    	switch( this.Stage )
		{
    		case GeneratingFileList:
    		{   	
    			this.StepGeneratingFileList( maxFilesToExamine );
    			break;
    		}
    		
    		case BuildingDigest:
    		{
    			this.StepBuildingDigest( maxBytesToRead );
    			break;
    		}
    		
    		default:
    		{
    			complete = true;
    			break;
    		}
		}

    	return complete;
    }
    
    public String CompleteSha1Signature()
    {
    	String sha1 = "";
    	
    	switch( this.Stage )
		{
    		case Complete:
    		{
    			byte[] sha1hash = this.getDigest();
	    		if( sha1hash != null )
	    		{
		    		sha1 = Base64.encodeToString( sha1hash, Base64.NO_WRAP );
		    		//Log.d(TAG, "APKVerify Success - Signature:" + sha1 + " Files Examined: " + this.FilesExamined + " Bytes Processed: " + this.BytesProcessed );
	    		}
    			break;
    		}
    		
    		default:
    		{
    			//Log.d(TAG, "APKVerify Failed - Files Examined: " + this.FilesExamined + " Bytes Processed: " + this.BytesProcessed );
    			break;
    		}
		}
    	
    	this.Stage = VerifyStage.Idle;
    	
    	return sha1;
    }
    
	public String SyncSha1Signature()
	{
		String sha1 = "";
    	boolean success = this.StartSha1Signature();
    	if( success == true )
    	{
	    	boolean complete = false;
	    	while( ( complete = this.StepSha1Signature(kBufferSize, 10, 20) ) == false )
	    	{
	    	}
	    	sha1 = this.CompleteSha1Signature();
    	}
    	return sha1;
	}
	
	private byte[] getDigest()
	{
		if( this.MD != null )
		{
			return this.MD.digest();
		}
		else if( this.MAC != null )
		{
			return this.MAC.doFinal();
		}
		else
		{
			return null;
		}
	}
	
	private void updateDigest( byte[] bytes, int offset, int length )
	{
		if( this.MD != null )
		{
			this.MD.update( bytes, offset, length );
		}
		else if( this.MAC != null )
		{
			this.MAC.update( bytes, offset, length );
		}
	}
		
}