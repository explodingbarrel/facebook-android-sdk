package com.explodingbarrel.iap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.Intent;

import com.unity3d.player.*;

import com.explodingbarrel.iap.util.IabHelper;
import com.explodingbarrel.iap.util.IabResult;
import com.explodingbarrel.iap.util.Inventory;
import com.explodingbarrel.iap.util.Purchase;
import com.explodingbarrel.iap.util.SkuDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;


public class MainActivity extends UnityPlayerActivity
{
    // Debug tag, for logging
    private static final String      TAG                = "MainActivity";
    private static final String     UNITY_PLUGIN_NAME = "iap_plugin_android";

    // The helper object
    IabHelper mHelper;
    String    mCallbacks = "iap_plugin_android";
    
    public static MainActivity         _this;
    public static int RC_REQUEST  = 1;
    public static boolean _supported = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        _this = this;
        super.onCreate(savedInstanceState);
    }
    
    
    public static void Setup( String publicKey, boolean debug )
    {
        if (_this != null && _this.mHelper == null)
        {
            _this.mHelper = new IabHelper(_this, publicKey);
            _this.mHelper.enableDebugLogging(debug);
            _this.mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        _supported = false;
                        return;
                    }
                    _supported = true;
                }
            });
        }
    }
    
    static void SendMessage( String plugin, String message, String data)
    {
        Log.d(TAG, "SendMessage: " + plugin + " " + message + " " + data);
        UnityPlayer.UnitySendMessage(plugin, message, data);
    }
    
    public static void PurchaseItem( String productId, String type, String payLoad )
    {
        if (_this != null)
        {
            final String p = productId;
            final String pl = payLoad;
            UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        _this.mHelper.launchPurchaseFlow(_this, p, RC_REQUEST, _this.mPurchaseFinishedListener, pl);
                    }
                    catch (IllegalStateException ex)
                    {
                         SendMessage(UNITY_PLUGIN_NAME, "OnItemPurchaseCanceled", "" );
                    }
                    
                }
            });
        }
    }

    public static void Enumerate( final String skus )
    {
        if (!_supported) {
            SendMessage(UNITY_PLUGIN_NAME, "OnIAPUnsupported", "");
            return;
        }
        
        UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.d(TAG, "Enumerate " + skus);
                
                String[] parts = skus.split(",");
                ArrayList<String> skuList = new ArrayList<String>();
                for ( int i = 0; i < parts.length; ++i ) {
                    skuList.add(parts[i]);
                }
                
                _this.mHelper.queryInventoryAsync(true, skuList, _this.mGotInventoryListener);
            }
        });
    }
    
    public static void CompletePurchase( String originalJson, String signature )
    {
        Log.d(TAG, "CompletePurchase " + originalJson);
        if (_this != null)
        {
            try
            {
                final Purchase purchase = new Purchase(IabHelper.ITEM_TYPE_INAPP, originalJson, signature);
                UnityPlayer.currentActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        _this.mHelper.consumeAsync(purchase, _this.mConsumeFinishedListener);
                    }
                });
            }
            catch (org.json.JSONException ex)
            {
            }
        
        }
    }
    
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            
            if (result.isFailure()) {
                
                if (result.getResponse() == 1 ) // BILLING_RESPONSE_RESULT_USER_CANCELED)
                {
                    SendMessage(UNITY_PLUGIN_NAME, "OnItemPurchaseCanceled", result.getMessage() );
                }
                else if ( result.getResponse() == 7 ) //BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED )
                {
                    // query the inventory
                    _this.mHelper.queryInventoryAsync(_this.mGotInventoryListener);
                }
                else
                {
                    SendMessage(UNITY_PLUGIN_NAME, "OnItemPurchaseFailed", result.getMessage() );
                }
            }
            else
            {
                SendMessage(UNITY_PLUGIN_NAME, "OnItemPurchased",  purchase.getOriginalJson() + "|" + purchase.getSignature() );
            }
            
            /*
             if (!verifyDeveloperPayload(purchase)) {
             complain("Error purchasing. Authenticity verification failed.");
             setWaitScreen(false);
             return;
             }*/
            
            
            //if (purchase.getSku().equals(SKU_GAS)) {
            //    // bought 1/4 tank of gas. So consume it.
            //    Log.d(TAG, "Purchase is gas. Starting gas consumption.");
            //    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            //}
        }
    };


    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                //complain("Failed to query inventory: " + result);
                return;
            }


            // got through all the purchases
            List<Purchase> purchases = inventory.getAllPurchases();
            for (Purchase purchase : purchases)
            {
                SendMessage(UNITY_PLUGIN_NAME, "OnItemPurchased",  purchase.getOriginalJson() + "|" + purchase.getSignature());
            }

            List<SkuDetails> skudetails = inventory.getAllSkus();
            if ( skudetails.size() > 0 ) {
                try  {
                    JSONArray array = new JSONArray();
                    for (SkuDetails sku : skudetails)
                    {
                        array.put( new JSONObject(sku.getJson()) );
                    }
                    SendMessage(UNITY_PLUGIN_NAME, "OnIAPSuppored", array.toString());
                }
                catch (JSONException ex){
                    SendMessage(UNITY_PLUGIN_NAME, "OnIAPSuppored", "");
                }
            }

            Log.d(TAG, "Query inventory was successful.");
        }
    };

     // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "onConsumeFinished(" + result + ")");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }


    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

 }
