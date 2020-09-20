package com.renatsayf.stockinsider.donate

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.*
import com.renatsayf.stockinsider.utils.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DonateViewModel @ViewModelInject constructor() : ViewModel()
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

        CoroutineScope(Dispatchers.Main).launch {
            skuDetailsResult = withContext(Dispatchers.IO) {
                billingClient.querySkuDetails(params.build())
            }
            val list = skuDetailsResult?.skuDetailsList
            if (!list.isNullOrEmpty())
            {
                val skulist: MutableList<SkuDetails> = mutableListOf()
                list.forEach { sku ->
                    skulist.add(sku)
                }
                skulist.sortByDescending { skuDetails -> skuDetails.priceAmountMicros }
                _priceList.value = skulist
            }
            return@launch
        }
    }

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
                        CoroutineScope(Dispatchers.Default).launch {
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