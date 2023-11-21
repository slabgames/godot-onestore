package com.slabgames.onestore;

import android.app.Activity;
import android.content.Intent;
import android.util.ArraySet;
import android.util.Log;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Set;

import android.app.Application.ActivityLifecycleCallbacks;

import androidx.annotation.NonNull;


import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;


import com.gaa.sdk.iap.AcknowledgeListener;
import com.gaa.sdk.iap.AcknowledgeParams;
import com.gaa.sdk.iap.ConsumeListener;
import com.gaa.sdk.iap.ConsumeParams;
import com.gaa.sdk.iap.IapResult;
import com.gaa.sdk.iap.IapResultListener;
import com.gaa.sdk.iap.ProductDetail;
import com.gaa.sdk.iap.ProductDetailsListener;
import com.gaa.sdk.iap.ProductDetailsParams;
import com.gaa.sdk.iap.PurchaseClient;
import com.gaa.sdk.iap.PurchaseClientStateListener;
import com.gaa.sdk.iap.PurchaseData;
import com.gaa.sdk.iap.PurchaseFlowParams;
import com.gaa.sdk.iap.PurchasesUpdatedListener;
import com.gaa.sdk.iap.QueryPurchasesListener;
import com.slabgames.onestore.utils.OnestoreUtils;


public class GodotOneStore extends GodotPlugin {

    private final String TAG = GodotOneStore.class.getName();
    private final HashMap<String, ProductDetail> productDetailsCache = new HashMap<>(); // sku → SkuDetails
    private PurchaseClient _purchaseClient;
    private boolean _purchaseClientReady;
    private HashMap<String,PurchaseData> _purchasesDataMap;

    private int _callbackId;
    private boolean calledStartConnection;

    public GodotOneStore(Godot godot) 
    {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "GodotOneStore";
    }

//    @NonNull
//    @Override
//    public List<String> getPluginMethods() {
//        return Arrays.asList(
//                "init",
//                "queryPurchases",
//                "acknowledgePurchase",
//                "consumePurchase",
//                "requestPurchase",
//                "queryProductDetailsAsync"
//        );
//    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            signals = new ArraySet<>();
        }
        signals.add(new SignalInfo("connected"));
        signals.add(new SignalInfo("disconnected"));
        signals.add(new SignalInfo("billing_resume"));
        signals.add(new SignalInfo("connect_error", Integer.class, String.class));
        signals.add(new SignalInfo("purchases_updated", Object[].class));
        signals.add(new SignalInfo("query_purchases_response", Object.class));
        signals.add(new SignalInfo("purchase_error", Integer.class, String.class));
        signals.add(new SignalInfo("sku_details_query_completed", Object[].class));
        signals.add(new SignalInfo("sku_details_query_error", Integer.class, String.class, String[].class));
        signals.add(new SignalInfo("price_change_acknowledged", Integer.class));
        signals.add(new SignalInfo("purchase_acknowledged", String.class));
        signals.add(new SignalInfo("purchase_acknowledgement_error", Integer.class, String.class, String.class));
        signals.add(new SignalInfo("purchase_consumed", String.class));
        signals.add(new SignalInfo("purchase_consumption_error", Integer.class, String.class, String.class));
//        signals.add(new SignalInfo("on_start_connection_success"));
//        signals.add(new SignalInfo("on_query_purchases_response",String[].class));
//        signals.add(new SignalInfo("on_handle_purchase",String.class));
//        signals.add(new SignalInfo("on_acknowledge_success",String.class));
//        signals.add(new SignalInfo("on_consume_success",String.class));
//        signals.add(new SignalInfo("on_product_details_response",Object[].class));

//        signals.add(new SignalInfo("connected"));
//        signals.add(new SignalInfo("disconnected"));
//        signals.add(new SignalInfo("billing_resume"));
//        signals.add(new SignalInfo("connect_error", Integer.class, String.class));
//        signals.add(new SignalInfo("purchases_updated", Object[].class));
//        signals.add(new SignalInfo("query_purchases_response", Object.class));
//        signals.add(new SignalInfo("purchase_error", Integer.class, String.class));
//        signals.add(new SignalInfo("sku_details_query_completed", Object[].class));
//        signals.add(new SignalInfo("sku_details_query_error", Integer.class, String.class, String[].class));
//        signals.add(new SignalInfo("price_change_acknowledged", Integer.class));
//        signals.add(new SignalInfo("purchase_acknowledged", String.class));
//        signals.add(new SignalInfo("purchase_acknowledgement_error", Integer.class, String.class, String.class));
//        signals.add(new SignalInfo("purchase_consumed", String.class));
//        signals.add(new SignalInfo("purchase_consumption_error", Integer.class, String.class, String.class));

        return signals;
    }

    /*
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(loggedInSignal);
    }
    */




    @Override
    public View onMainCreate(Activity activity) {
        _purchaseClientReady = false;
        _purchasesDataMap = new HashMap<String,PurchaseData>();

        final Activity act = activity;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {                
                act.getApplication().registerActivityLifecycleCallbacks(new OneStoreLifecycleCallbacks());
                Log.d(TAG,"OneStore plugin inited onCreate");
            }
        });
        return null;
    }

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(IapResult iapResult, List<PurchaseData> purchases) {
            // To be implemented in a later section.
            if (iapResult.isSuccess() && purchases != null) {
                emitSignal("purchases_updated", (Object)OnestoreUtils.convertPurchaseListToDictionaryObjectArray(purchases));
//                for (PurchaseData purchase : purchases) {
//                    if (purchase.getPurchaseState() == PurchaseData.PurchaseState.PURCHASED) {
//                        emitSignal("purchases_updated", (Object)OnestoreUtils.convertPurchaseListToDictionaryObjectArray(list));
//                        emitSignal("on_handle_purchase", purchase.getProductId());
////                    handlePurchase(purchase);
//                    }
//                }
            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_UPDATE) {
                // PurchaseClient by calling the launchUpdateOrInstallFlow() method.
                _purchaseClient.launchUpdateOrInstallFlow(getActivity(),iapResultListener);

            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_LOGIN) {
                // PurchaseClient by calling the launchLoginFlow() method.
                startConnection();
            } else {
                emitSignal("purchase_error", iapResult.getResponseCode(), iapResult.getMessage());
                // Handle any other error codes.
                Log.e(TAG, "Error in PurchasesUpdatedListener");
            }
        }
    };

    private IapResultListener iapResultListener = new IapResultListener() {
        @Override
        public void onResponse(IapResult iapResult) {
            if(iapResult.isSuccess())
            {
                startConnection();
            }
        }
    };


    QueryPurchasesListener queryPurchasesListener = new QueryPurchasesListener() {
        @Override
        public void onPurchasesResponse(IapResult iapResult, List<PurchaseData> purchases) {
            if (iapResult.isSuccess() && purchases != null) {
                Dictionary returnValue = new Dictionary();
                if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_OK) {
                    returnValue.put("status", 0); // OK = 0
                    returnValue.put("purchases", OnestoreUtils.convertPurchaseListToDictionaryObjectArray(purchases));
                } else {
                    returnValue.put("status", 1); // FAILED = 1
                    returnValue.put("response_code", iapResult.getResponseCode());
                    returnValue.put("debug_message", iapResult.getMessage());
                }
                emitSignal("query_purchases_response", (Object)returnValue);

            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_UPDATE) {
                // PurchaseClient by calling the launchUpdateOrInstallFlow() method.
                _purchaseClient.launchUpdateOrInstallFlow(getActivity(), iapResultListener);
            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_LOGIN) {
                // PurchaseClient by calling the launchLoginFlow() method.
                startConnection();
            } else {
                Dictionary returnValue = new Dictionary();
                returnValue.put("status", 1); // FAILED = 1
                returnValue.put("response_code", iapResult.getResponseCode());
                returnValue.put("debug_message", iapResult.getMessage());
                emitSignal("query_purchases_response", (Object)returnValue);
                // Handle any other error codes.
                Log.e(TAG,"error handling query puchase");
            }
        }

    };



    private void handlePurchase(PurchaseData purchase) {

        if (purchase.getPurchaseState() == PurchaseData.PurchaseState.PURCHASED) {
                emitSignal("on_handle_purchase", purchase.getProductId());
//            GodotLib.calldeferred(_callbackId,"on_handle_purchase",new Object[]{purchase.getProductId()});

                // Purchase retrieved from PurchaseClient#queryPurchasesAsync or your PurchasesUpdatedListener.
                //        PurchaseData purchase =

                // Verify the purchase.
                // Ensure entitlement was not already granted for this purchaseToken.
                // Grant entitlement to the user.

        }

    }



    // Public methods
    @UsedByGodot
    public boolean isReady() {
        return this._purchaseClient.isReady();
    }

    @UsedByGodot
    public int getConnectionState() {
        return _purchaseClient.getConnectionState();
    }

    @UsedByGodot
    public void startConnection() {
        calledStartConnection = true;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _purchaseClient.startConnection(new PurchaseClientStateListener() {
                    @Override
                    public void onSetupFinished(IapResult iapResult) {
                        if(iapResult.isSuccess())
                        {
                            // The PurchaseClient is ready. You can query purchases here.
                            Log.d(TAG,"One Store Purchase Client inited");
                            emitSignal("on_start_connection_success");
//                    GodotLib.calldeferred(_callbackId,"on_start_connection_success",new Object[]{});
                            _purchaseClientReady = true;

                        }
                    }

                    @Override
                    public void onServiceDisconnected() {
                        // Try to restart the connection on the next request to
                        // PurchaseClient by calling the startConnection() method.
                        Log.d(TAG,"One Store Purchase Client disconnected");
                        _purchaseClientReady = false;
                        startConnection();
                    }
                });
            }
        });

    }


    @UsedByGodot
    public void endConnection() {
        _purchaseClient.endConnection();
    }

    @UsedByGodot
    public void init(final String licenseKey)
    {
//        _callbackId = callback_id;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                _purchaseClient = PurchaseClient.newBuilder(getActivity())
                        .setListener(purchasesUpdatedListener)
                        .setBase64PublicKey(licenseKey) // optional
                        .build();

                getActivity().getApplication().registerActivityLifecycleCallbacks(new OneStoreLifecycleCallbacks());


                Log.d(TAG,"One Store plugin init on Java");
            }
        });
    }

    @UsedByGodot
    public  void queryPurchases(String type)
    {
        _purchaseClient.queryPurchasesAsync(type, queryPurchasesListener);
    }

    @UsedByGodot
    public void acknowledgePurchase(final String purchaseID)
    {
        PurchaseData purchaseData = _purchasesDataMap.get(purchaseID);
        if (!purchaseData.isAcknowledged())
        {
            AcknowledgeParams acknowledgeParams = AcknowledgeParams.newBuilder().setPurchaseData(purchaseData).build();
            _purchaseClient.acknowledgeAsync(acknowledgeParams, new AcknowledgeListener() {
                @Override
                public void onAcknowledgeResponse(IapResult iapResult, PurchaseData purchaseData) {
                    // PurchaseClient by calling the queryPurchasesAsync() method.
                    if(iapResult.isSuccess())
                    {
                        emitSignal("purchase_acknowledged", purchaseID);
//                        GodotLib.calldeferred(_callbackId,"on_acknowledge_success",new Object[]{purchaseID});
                    }
                    else {
                        emitSignal("purchase_acknowledgement_error", iapResult.getResponseCode(), iapResult.getMessage(), purchaseID);
                    }
                }
            });
        }
    }

    @UsedByGodot
    public  void consumePurchase(final String purchaseID)
    {
        PurchaseData purchaseData = _purchasesDataMap.get(purchaseID);
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseData(purchaseData).build();
        _purchaseClient.consumeAsync(consumeParams, new ConsumeListener() {
            @Override
            public void onConsumeResponse(IapResult iapResult, PurchaseData purchaseData) {
                // Process the result.
                if(iapResult.isSuccess())
                {
                    if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_OK) {
                        emitSignal("purchase_consumed", purchaseID);
                    }
//                    GodotLib.calldeferred(_callbackId,"on_consume_success",new Object[]{purchaseID});
                }
                else
                {
                    emitSignal("purchase_consumption_error", iapResult.getResponseCode(), iapResult.getMessage(), purchaseID);
                }
            }
        });
    }

    @UsedByGodot
    public void purchase(String productId)
    {
        PurchaseFlowParams purchaseFlowParams = PurchaseFlowParams.newBuilder()
              .setProductId(productId)
              .setProductType(PurchaseClient.ProductType.INAPP)
//              .setDeveloperPayload(devPayload)    // optional
//              .setQuantity(qty)                     // optional
//              .setProductName(productName)                 // optional
//              .setGameUserId(gameUserId)                  // optional
//              .setPromotionApplicable(false)      // optional
              .build();

        _purchaseClient.launchPurchaseFlow(getActivity(), purchaseFlowParams);
    }


    @UsedByGodot
    public void querySkuDetails(final String[] productIdArray, final String type)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> productIdList = Arrays.asList(productIdArray);
                ProductDetailsParams productDetailsParams = ProductDetailsParams.newBuilder()
                .setProductIdList(productIdList)
                .setProductType(type)
                .build();

                _purchaseClient.queryProductDetailsAsync(productDetailsParams, new ProductDetailsListener() {
                    @Override
                    public void onProductDetailsResponse(IapResult iapResult, List<ProductDetail> list) {
                        if (iapResult.isSuccess()) {
                            for (ProductDetail productDetails : list) {
                                productDetailsCache.put(productDetails.getProductId(), productDetails);
                            }
                            emitSignal("sku_details_query_completed", (Object)OnestoreUtils.convertProductDetailsListToDictionaryObjectArray(list));
                        } else {
                            emitSignal("sku_details_query_error", iapResult.getResponseCode(), iapResult.getMessage(), list);
                        }
                        // Process the result.
//                        emitSignal("on_product_details_response",list.toArray());
//                        GodotLib.calldeferred(_callbackId,"on_product_details_response",new Object[]{list.toArray()});
                    }
                    
                });
            }
        });
        
    }

    

    private static final class OneStoreLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated (Activity activity, 
                Bundle savedInstanceState) {
            
        }

        @Override
        public void onActivityStarted( Activity activity) {

        }

        @Override
         public void onActivityResumed(Activity activity) {

         }

         @Override
         public void onActivityPaused(Activity activity) {
             
         }

        @Override
        public void onActivityStopped( Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState( Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed( Activity activity) {

        }

        //...
     }
    



    // Internal methods

    @Override
    public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
    }
}
