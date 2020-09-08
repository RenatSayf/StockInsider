package com.renatsayf.stockinsider.repository

import com.renatsayf.stockinsider.models.Deal
import com.renatsayf.stockinsider.models.SearchSet
import java.util.*
import kotlin.collections.ArrayList

interface IDataRepository
{
    fun getDealList(set: SearchSet) : io.reactivex.Observable<ArrayList<Deal>>
}