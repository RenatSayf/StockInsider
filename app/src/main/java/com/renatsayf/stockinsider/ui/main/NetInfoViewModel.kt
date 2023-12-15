package com.renatsayf.stockinsider.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renatsayf.stockinsider.models.NetInfo
import com.renatsayf.stockinsider.network.IApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NetInfoViewModel @Inject constructor(
    private  val repository: IApi
): ViewModel() {

    private suspend fun getNetInfoAsync(): Deferred<NetInfo?> {
        return coroutineScope {
            async {
                val response = repository.getNetworkInfo()
                if (response.isSuccessful) {
                    val body = response.body()
                    val info = if (body != null) {
                        val data = try {
                            Json.decodeFromString<NetInfo>(body)
                        } catch (e: Exception) {
                            null
                        }
                        data
                    } else {
                        null
                    }
                    info
                }
                else {
                    null
                }
            }
        }
    }

    fun getCountryCode() {
        viewModelScope.launch {
            val netInfo = getNetInfoAsync().await()
            netInfo?.let {
                _countryCode.value = Result.success(it.countryCode)
            }?: run {
                val countryCode = Locale.getDefault().country
                _countryCode.value = Result.success(countryCode)
            }
        }
    }

    private var _countryCode = MutableLiveData<Result<String>>(Result.failure(Exception()))
    val countryCode: LiveData<Result<String>> = _countryCode

}