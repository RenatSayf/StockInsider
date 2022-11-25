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

    fun querySkuDetails(billingClient: BillingClient)
    {
        var skuDetailsResult: SkuDetailsResult?
        val skuList = ArrayList<String>()
        skuList.add("user_donation_50")
        skuList.add("user_donation100")
        skuList.add("user_donation200")
        skuList.add("user_donation300")
        skuList.add("user_donation400")
        skuList.add("user_donation_500")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        viewModelScope.launch {
            skuDetailsResult = withContext(Dispatchers.IO) {
                billingClient.querySkuDetails(params.build())
            }
            val list = skuDetailsResult?.skuDetailsList
            if (!list.isNullOrEmpty())
            {
                val details = list.map {
                    it
                }.toMutableList()

                details.sortByDescending { skuDetails -> skuDetails.priceAmountMicros }
                _priceList.value = details
            }
            return@launch
        }
    }

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
                donateList?.value = products
            }
        }
    }

    private fun buildProduct(id: String): QueryProductDetailsParams.Product {
        return QueryProductDetailsParams.Product.newBuilder().apply {
            setProductId(id)
            setProductType(BillingClient.ProductType.INAPP)
        }.build()
    }

    var donateList: MutableLiveData<List<ProductDetails>>? = null
        private set

    private val _priceList = MutableLiveData<MutableList<SkuDetails>>().apply {
        value = priceList?.value
    }
    var priceList: LiveData<MutableList<SkuDetails>> = _priceList

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
                    eventPurchased.value = Event(outToken)
                }
                return@consumeAsync
            }
        }
    }
}