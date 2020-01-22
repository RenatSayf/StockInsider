package com.renatsayf.stockinsider.models

import android.os.Parcel
import android.os.Parcelable

data class Deal(val filingDate : String?) : Parcelable
{
    var filingDateRefer : String? = ""
    var tradeDate: String? = ""
    var ticker : String? = ""
    var tickerRefer : String? = ""
        set(value)
        {
            field = "https://www.profitspi.com/stock/stock-charts.ashx?chart=$value"
        }
    var company : String? = ""
    var companyRefer : String? = ""
        set(value)
        {
            field = "http://openinsider.com/$value"
        }
    var insiderName : String? = ""
    var insiderNameRefer : String? = ""
        set(value)
        {
            field = "http://openinsider.com$value"
        }
    var insiderTitle : String? = ""
    var tradeType : String? = ""
    var price : String? = ""
    var qty : Double? = 0.0
    var owned : Double? = 0.0
    var deltaOwn : String? = ""
    var volume : Double? = 0.0

    constructor(parcel : Parcel) : this(parcel.readString())
    {
        filingDateRefer = parcel.readString()
        tradeDate = parcel.readString()
        ticker = parcel.readString()
        company = parcel.readString()
        insiderName = parcel.readString()
        insiderTitle = parcel.readString()
        tradeType = parcel.readString()
        price = parcel.readString()
        qty = parcel.readValue(Double::class.java.classLoader) as? Double
        owned = parcel.readValue(Double::class.java.classLoader) as? Double
        deltaOwn = parcel.readString()
        volume = parcel.readValue(Double::class.java.classLoader) as? Double
    }

    override fun writeToParcel(parcel : Parcel, flags : Int)
    {
        parcel.writeString(filingDate)
        parcel.writeString(filingDateRefer)
        parcel.writeString(tradeDate)
        parcel.writeString(ticker)
        parcel.writeString(company)
        parcel.writeString(insiderName)
        parcel.writeString(insiderTitle)
        parcel.writeString(tradeType)
        parcel.writeString(price)
        parcel.writeValue(qty)
        parcel.writeValue(owned)
        parcel.writeString(deltaOwn)
        parcel.writeValue(volume)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Deal>
    {
        override fun createFromParcel(parcel : Parcel) : Deal
        {
            return Deal(parcel)
        }

        override fun newArray(size : Int) : Array<Deal?>
        {
            return arrayOfNulls(size)
        }
    }
}