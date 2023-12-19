@file:Suppress("ObjectLiteralToLambda", "UNUSED_ANONYMOUS_PARAMETER")

package com.renatsayf.stockinsider.ui.donate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.renatsayf.stockinsider.ui.settings.BILLING_CLIENT_IS_NOT_READY
import com.renatsayf.stockinsider.ui.settings.PURCHASES_NOT_FOUND
import com.renatsayf.stockinsider.utils.Event
import com.renatsayf.stockinsider.utils.printStackTraceIfDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject


@HiltViewModel
class DonateViewModel @Inject constructor(app: Application) : AndroidViewModel(app), PurchasesUpdatedListener {

    val eventPurchased : MutableLiveData<Event<String>> = MutableLiveData()

    private val productList = listOf(
        buildProduct("user_donation_50"),
        buildProduct("user_donation100"),
        buildProduct("user_donation200"),
        buildProduct("user_donation300"),
        buildProduct("user_donation400"),
        buildProduct("user_donation_500"),
        buildProduct("user_donation_1000")
    )

    val billingClient = BillingClient.newBuilder(app)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails(billingClient)
                    getPurchasesHistory()
                }
            }
            override fun onBillingServiceDisconnected() {
                _donateList.postValue(listOf())
            }
        })
    }

    fun queryProductDetails(billingClient: BillingClient) {


        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        billingClient.queryProductDetailsAsync(params.build()) { billingResult, products ->
            //val code = billingResult.responseCode
            if (products.isNotEmpty()) {
                products.sortByDescending { p -> p.oneTimePurchaseOfferDetails?.formattedPrice }
                _donateList.postValue(products)
            }
        }
    }

    private fun buildProduct(id: String): QueryProductDetailsParams.Product {
        return QueryProductDetailsParams.Product.newBuilder().apply {
            setProductId(id)
            setProductType(BillingClient.ProductType.INAPP)
        }.build()
    }

    private var _donateList = MutableLiveData<List<ProductDetails>>()
    var donateList: LiveData<List<ProductDetails>> = _donateList

    fun buildBillingFlowParams(price: String): LiveData<BillingFlowParams?> {
        val flowParams = MutableLiveData<BillingFlowParams>(null)
        val value = _donateList.value
        if(!value.isNullOrEmpty())
        {
            val productDetails = value.first { details ->
                details.oneTimePurchaseOfferDetails?.formattedPrice == price
            }

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
            flowParams.value = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
        }
        return flowParams
    }

    private fun handlePurchase(billingClient: BillingClient, purchase: Purchase)
    {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK)
            {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED)
                {
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                            }
                        }
                    }
                    eventPurchased.postValue(Event(outToken))
                }
                return@consumeAsync
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(billingClient, purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            eventPurchased.postValue(Event(null))
        }
    }

    fun getPurchasesHistory() {

        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val ready = billingClient.isReady
        if (ready) {
            billingClient.queryPurchaseHistoryAsync(params, object : PurchaseHistoryResponseListener {
                private val jsonParser = Json {
                    ignoreUnknownKeys = true
                }

                override fun onPurchaseHistoryResponse(
                    result: BillingResult,
                    historyRecords: MutableList<PurchaseHistoryRecord>?
                ) {
                    historyRecords?.let{ records ->
                        val purchases = records.mapNotNull {
                            val json = it.originalJson
                            try {
                                jsonParser.decodeFromString<UserPurchase>(json)
                            } catch (e: Exception) {
                                e.printStackTraceIfDebug()
                                null
                            }
                        }.filter {
                            it.productId.contains("user_donation", ignoreCase = true)
                        }
                        if (purchases.isNotEmpty()) {
                            _purchases.postValue(Result.success(purchases))
                        }
                        else {
                            _purchases.postValue(Result.failure(Throwable(PURCHASES_NOT_FOUND)))
                        }
                    }?: run {
                        _purchases.postValue(Result.failure(Throwable(PURCHASES_NOT_FOUND)))
                    }
                }
            })
        }
        else {
            val exception = Exception(BILLING_CLIENT_IS_NOT_READY)
            exception.printStackTraceIfDebug()
            _purchases.value = Result.failure(exception)
        }
    }

    private var _purchases = MutableLiveData<Result<List<UserPurchase>>>()
    val purchases: LiveData<Result<List<UserPurchase>>> = _purchases


}