package com.slabgames.onestore;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.view.View;

import java.util.List;
import java.util.Arrays;

import android.app.Application.ActivityLifecycleCallbacks;

import androidx.annotation.NonNull;


import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;


import com.gaa.sdk.iap.AcknowledgeListener;
import com.gaa.sdk.iap.AcknowledgeParams;
import com.gaa.sdk.iap.ConsumeListener;
import com.gaa.sdk.iap.ConsumeParams;
import com.gaa.sdk.iap.IapResult;
import com.gaa.sdk.iap.ProductDetail;
import com.gaa.sdk.iap.ProductDetailsListener;
import com.gaa.sdk.iap.ProductDetailsParams;
import com.gaa.sdk.iap.PurchaseClient;
import com.gaa.sdk.iap.PurchaseClientStateListener;
import com.gaa.sdk.iap.PurchaseData;
import com.gaa.sdk.iap.PurchaseFlowParams;
import com.gaa.sdk.iap.PurchasesUpdatedListener;
import com.gaa.sdk.iap.QueryPurchasesListener;


public class GodotOneStore extends GodotPlugin {

    private final String TAG = GodotOneStore.class.getName();
    private PurchaseClient purchaseClient;
    private boolean _purchaseClientReady;

    public GodotOneStore(Godot godot) 
    {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "GodotOneStore";
    }

    @NonNull
    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList(
                "init",
                "queryPurchase"


        );
    }

    /*
    @Override
    public Set<SignalInfo> getPluginSignals() {
        return Collections.singleton(loggedInSignal);
    }
    */
    public void init(final String licenseKey)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                PurchaseClient purchaseClient = PurchaseClient.newBuilder(getActivity())
                        .setListener(purchasesUpdatedListener)
                        .setBase64PublicKey(licenseKey) // optional
                        .build();

                getActivity().getApplication().registerActivityLifecycleCallbacks(new OneStoreLifecycleCallbacks());



                Log.d(TAG,"One Store plugin init on Java");
            }
        });
    }

    private void startConnection() {
        purchaseClient.startConnection(new PurchaseClientStateListener() {
            @Override
            public void onSetupFinished(IapResult iapResult) {
                if(iapResult.isSuccess())
                {
                    // The PurchaseClient is ready. You can query purchases here.
                    Log.d(TAG,"One Store Purchase Client inited");
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

    @Override
    public View onMainCreate(Activity activity) {
        _purchaseClientReady = false;
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
                for (PurchaseData purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_UPDATE) {
                // PurchaseClient by calling the launchUpdateOrInstallFlow() method.


            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_LOGIN) {
                // PurchaseClient by calling the launchLoginFlow() method.
                startConnection();
            } else {
                // Handle any other error codes.
            }
        }
    };

    QueryPurchasesListener queryPurchasesListener = new QueryPurchasesListener() {
        @Override
        public void onPurchasesResponse(IapResult iapResult, List<PurchaseData> purchases) {
            if (iapResult.isSuccess() && purchases != null) {

                for (PurchaseData purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_UPDATE) {
                // PurchaseClient by calling the launchUpdateOrInstallFlow() method.
            } else if (iapResult.getResponseCode() == PurchaseClient.ResponseCode.RESULT_NEED_LOGIN) {
                // PurchaseClient by calling the launchLoginFlow() method.
            } else {
                // Handle any other error codes.
            }
        }

    };

    public  void queryPurchase()
    {
        purchaseClient.queryPurchasesAsync(PurchaseClient.ProductType.INAPP, queryPurchasesListener);

    }


    private void handlePurchase(PurchaseData purchase) {

        if (purchase.getPurchaseState() == PurchaseData.PurchaseState.PURCHASED) {
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

        else {
            // Purchase retrieved from PurchaseClient#queryPurchasesAsync or your PurchasesUpdatedListener.
            //        PurchaseData purchase =

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

    }



    // Public methods



//    public void launchManageSubscription(@Nullable PurchaseData purchaseData) {
//        SubscriptionParams subscriptionParams = null;
//        if (purchaseData != null) {
//                subscriptionParams = SubscriptionParams.newBuilder()
//                .setPurchaseData(purchaseData)
//                .build();
//        }
//        GodotOneStore purchaseClient = null;
//        purchaseClient.launchManageSubscription(getActivity(), subscriptionParams);
//    }

    public void requestPurchase(String productId, String productType, String devPayload, int qty, String productName, String gameUserId)
    {
        PurchaseFlowParams purchaseFlowParams = PurchaseFlowParams.newBuilder()
              .setProductId(productId)
              .setProductType(productType)
              .setDeveloperPayload(devPayload)    // optional
              .setQuantity(qty)                     // optional
              .setProductName(productName)                 // optional
              .setGameUserId(gameUserId)                  // optional
              .setPromotionApplicable(false)      // optional
              .build();

        purchaseClient.launchPurchaseFlow(getActivity(), purchaseFlowParams);
    }

    public void queryProductDetailsAsync()
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<String> productIdList = null;
                ProductDetailsParams productDetailsParams = ProductDetailsParams.newBuilder()
                .setProductIdList(productIdList)
                .setProductType(PurchaseClient.ProductType.ALL)
                .build();

                purchaseClient.queryProductDetailsAsync(productDetailsParams, new ProductDetailsListener() {
                    @Override
                    public void onProductDetailsResponse(IapResult iapResult, List<ProductDetail> list) {
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
    



    // Internal methods

    @Override
    public void onMainActivityResult (int requestCode, int resultCode, Intent data)
    {
    }
}
