package com.slabgames.onestore;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Application.ActivityLifecycleCallbacks;

import androidx.annotation.NonNull;


import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;
import org.godotengine.godot.GodotLib;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;

public class GodotOneStore extends GodotPlugin {

    private final String TAG = GodotOneStore.class.getName();

    public GodotOneStore(Godot godot) 
    {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "GodotOneStore";
    }

    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList(
                "init"

        );
    }

    /*
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(loggedInSignal);
    }
    */

    @Override
    public View onMainCreate(Activity activity) {
        final Activity act = activity;
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {                
                act.getApplication().registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
                Log.d(TAG,"OneStore plugin inited onCreate");
            }
        });
        return null;
    }

    QueryPurchasesListener queryPurchasesListener = new QueryPurchasesListener() {
        @Override
        public void onPurchasesResponse(IapResult iapResult, list<PurchaseData> purchases) { 
            if (iapResult.isSuccess() && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (iapResult.getResponseCode() == ResponseCode.NEED_UPDATE) {
                // PurchaseClient by calling the launchUpdateOrInstallFlow() method.
            } else if (iapResult.getResponseCode() == ReponseCode.NEED_LOGIN) {
                // PurchaseClient by calling the launchLoginFlow() method.
            } else {
                 // Handle any other error codes.
            }
        }
    };

    purchaseClient.queryPurchasesAsync(ProductType.INAPP, queryPurchasesListener);

    private void handlePurchase(PurchaseData purchase) {
        // Purchase retrieved from PurchaseClient#queryPurchasesAsync or your PurchasesUpdatedListener.
        PurchaseData purchase = ...
          
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseData(purchase).build();
        purchaseClient.consumeAsync(consumeParams, new ConsumeListener() {
            @Override
            public void onConsumeResponse(IapResult iapResult, PurchaseData purchaseData) {
                 // Process the result.
            }
        });
    }

    // Purchase retrieved from PurchaseClient#queryPurchasesAsync or your PurchasesUpdatedListener.
    private void handlePurchase(purchase: PurchaseData) {
        if (purchase.getPurchaseState() == PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgeParams acknowledgeParams = AcknowledgeParams.newBuilder().setPurchaseData(purchase).build();
                purchaseClient.acknowledgeAsync(acknowledgeParams, new AcknowledgeListener() {
                    @Override
                    public void onAcknowledgeResponse(IapResult iapResult, PurchaseData purchaseData) {
                        // PurchaseClient by calling the queryPurchasesAsync() method.
                    }
                });
            }
        }
    }


    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener {
         @Override
         public void onPurchasesUpdated(IapResult iapResult, List) {
             // To be implemented in a later section.
            if (iapResult.isSuccess() && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase);
                }
            } else if (iapResult.getResponseCode() == ResponseCode.NEED_UPDATE) {
                // PurchaseClient by calling the launchUpdateOrInstallFlow() method.
            } else if (iapResult.getResponseCode() == ReponseCode.NEED_LOGIN) {
                // PurchaseClient by calling the launchLoginFlow() method.
            } else {
                // Handle any other error codes.
            }
         }
    };

    // Public methods

    public void init(final String token, final boolean ProductionMode)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                
                private PurchaseClient purchaseClient = PurchaseClient.newBuilder(getActivity())
                   .setListener(purchasesUpdatedListener)
                   //.setBase64PublicKey(/*your license key*/) // optional
                   .build();
                
                getActivity().getApplication().registerActivityLifecycleCallbacks(new OneStoreLifecycleCallbacks());


                purchaseClient.startConnection(new PurchaseClientStateListener {
                    @Override
                    public void onSetupFinished(IapResult iapResult) {
                        if (iapResult.isSuccess()) {
                            purchaseClient.startConnection(new PurchaseClientStateListener {
                                @Override
                                public void onSetupFinished(IapResult iapResult) {
                                    if (iapResult.isSuccess()) {
                                        // The PurchaseClient is ready. You can query purchases here.
                                    }
                                }

                                @Override
                                public void onServiceDisconnected() {
                                    // Try to restart the connection on the next request to
                                    // PurchaseClient by calling the startConnection() method.
                                }
                            });
                        }
                    }

                    @Override
                    public void onServiceDisconnected() {
                        // Try to restart the connection on the next request to
                        // PurchaseClient by calling the startConnection() method.
                    }
                });
                Log.d(TAG,"One Store plugin inited on Java");
            }
        });
    }

    public void launchManageSubscription(@Nullable PurchaseData purchaseData) {
        SubscriptionParams subscriptionParams = null;
        if (purchaseData != null) {
            subscriptionParams = SubscriptionParams.newBuilder()
                .setPurchaseData(purchaseData)
                .build();
        }
        purchaseClient.launchManageSubscription(mActivity, subscriptionParams);
    }

    public void requestPurchase()
    {
        PurchaseFlowParams purchaseFlowParams = PurchaseFlowParams.newBuilder()
              .setProductId(productId)
              .setProductType(productType)
              .setDeveloperPayload(devPayload)    // optional
              .setQuantity(1)                     // optional
              .setProductName("")                 // optional
              .setGameUserId("")                  // optional
              .setPromotionApplicable(false)      // optional
              .build();

        purchaseClient.launchPurchaseFlow(activity, purchaseFlowParams);
    }

    public void queryProductDetailsAsync()
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProductDetailsParams productDetailsParams = ProductDetailsParams.newBuilder()
                .setProductIdList(productIdList)
                .setProductType(ProductType.INAPP)
                .build();

                purchaseClient.queryProductDetailsAsync(productDetailsParams, new ProductDetailsListener() {
                    @Override
                    public void onProductDetailsResponse(IapResult iapResult, List<ProductDetail>) {
                        // Process the result. 
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
    
    





    public void track_revenue(final String revenue, final String currency, final String signature, final String originalJson, final String public_key)
    {
        AdjustEvent adjustEvent = new AdjustEvent(signature);
        adjustEvent.setRevenue(Double.parseDouble(revenue), currency);
        Adjust.trackEvent(adjustEvent);
    }


    // Internal methods

    @Override
    public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
    }
}
