package com.renatsayf.stockinsider.ui.donate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import com.renatsayf.stockinsider.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class DonateViewModel @Inject constructor() : ViewModel()
{
    val eventPurchased : MutableLiveData<Event<String>> = MutableLiveData()

    fun queryProductDetails(billingClient: BillingClient) {

        val productList = listOf(
            buildProduct("user_donation_50"),
            buildProduct("user_donation100"),
            buildProduct("user_donation200"),
            buildProduct("user_donation300"),
            buildProduct("user_donation400"),
            buildProduct("user_donation_500")
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        billingClient.queryProductDetailsAsync(params.build()) { billingResult, products ->
            val code = billingResult.responseCode
            if (products.isNotEmpty()) {
                products.sortByDescending { p -> p.name }
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

    fun handlePurchase(billingClient: BillingClient, purchase: Purchase)
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
                        viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                            }
                        }
                    }
                    eventPurchased.postValue(Event(outToken))
                }
                return@consumeAsync
            }
        }
    }
}