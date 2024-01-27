package com.renatsayf.stockinsider.ui.referral

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.renatsayf.stockinsider.firebase.FireBaseConfig
import com.renatsayf.stockinsider.models.ResultData
import com.renatsayf.stockinsider.ui.referral.models.Brokers
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ReferralViewModel @Inject constructor() : ViewModel() {

    fun fetchPartners(remoteConfig: FireBaseConfig) {
        val brokers = remoteConfig.partners
        if (brokers.list.isNotEmpty()) {
            _brokers.value = ResultData.Success(brokers)
        } else {
            _brokers.value = ResultData.Error("List is empty")
        }
    }

    private var _brokers = MutableLiveData<ResultData<Brokers>>()
    val brokers: LiveData<ResultData<Brokers>> = _brokers
}