package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import com.renatsayf.stockinsider.network.SearchRequest
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import javax.inject.Inject

class DataRepositoryImpl @Inject constructor(val searchRequest: SearchRequest) : IDataRepository
{
    override fun getDealList(set: SearchSet): Observable<ArrayList<Deal>>
    {
        return searchRequest.getTradingScreen(set)
    }
}