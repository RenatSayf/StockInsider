package com.renatsayf.stockinsider.donate

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.*
import com.renatsayf.stockinsider.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DonateViewModel @ViewModelInject constructor() : ViewModel()
{
    private var skuDetails: SkuDetails? = null

    init
    {

    }

    fun querySkuDetails(billingClient: BillingClient)
    {
        var skuDetailsResult: SkuDetailsResult?
        val skuList = ArrayList<String>()
        skuList.add("user_donation")
        skuList.add("user_donation50")
        skuList.add("user_donation100")
        skuList.add("user_donation200")
        skuList.add("user_donation300")
        skuList.add("user_donation400")
        skuList.add("user_donation500")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        CoroutineScope(Dispatchers.Main).launch {
            skuDetailsResult = withContext(Dispatchers.IO) {
                billingClient.querySkuDetails(params.build())
            }
            val list = skuDetailsResult?.skuDetailsList
            if (!list.isNullOrEmpty())
            {
                val _list: MutableList<SkuDetails> = mutableListOf()
                list.forEach { sku ->
                    _list.add(sku)
                }
                _list.sortByDescending { skuDetails -> skuDetails.zza() }
                _priceList.value = _list
            }
            return@launch
        }
    }

    private val _priceList = MutableLiveData<MutableList<SkuDetails>>().apply {
        value = priceList?.value
    }

    var priceList: LiveData<MutableList<SkuDetails>> = _priceList


}