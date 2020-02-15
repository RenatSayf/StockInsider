package com.renatsayf.stockinsider.models

import android.os.Parcel
import android.os.Parcelable

data class Deal(val filingDate : String?) : Parcelable
{
    var filingDateRefer : String? = ""
    var tradeDate: String? = ""
    var ticker : String? = ""
        set(value)
        {
            field = value
            tickerRefer = "https://www.profitspi.com/stock/stock-charts.ashx?chart=$value"
        }
    var tickerRefer : String? = ""
        private set
    var company : String? = ""
        set(value)
        {
            field = value
            companyRefer = "http://openinsider.com/$value"
        }
    var companyRefer : String? = ""
        private set
    var insiderName : String? = ""
    var insiderNameRefer : String? = ""
        set(value)
        {
            field = "http://openinsider.com$value"
        }
    var insiderTitle : String? = ""
    var tradeType : String? = ""
        set(value)
        {
            field = value
            when(value)
            {
                "P - Purchase" -> tradeTypeInt = 1
                "S - Sale" -> tradeTypeInt = -1
                "S - Sale+OE" -> tradeTypeInt = -2
            }
        }
    var tradeTypeInt : Int = 0
        private set
    var price : String? = ""
    var qty : Double? = 0.0
    var owned : Double? = 0.0
    var deltaOwn : String? = ""
    var volumeStr : String = ""
        set(value)
        {
            field = value
            val regex = Regex("""\D""")
            val strNum = field.replace(regex, "")
            volume = strNum.toDouble()
        }
    var volume : Double = 0.0
        private set
    var error : String? = ""

    constructor(parcel : Parcel) : this(parcel.readString())
    {
        filingDateRefer = parcel.readString()
        tradeDate = parcel.readString()
        ticker = parcel.readString()
        company = parcel.readString()
        insiderName = parcel.readString()
        insiderTitle = parcel.readString()
        tradeType = parcel.readString()
        tradeTypeInt = parcel.readInt()
        price = parcel.readString()
        qty = parcel.readValue(Double::class.java.classLoader) as? Double
        owned = parcel.readValue(Double::class.java.classLoader) as? Double
        deltaOwn = parcel.readString()
        volume = parcel.readValue(Double::class.java.classLoader) as Double
        error = parcel.readString()
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
        parcel.writeInt(tradeTypeInt)
        parcel.writeString(price)
        parcel.writeValue(qty)
        parcel.writeValue(owned)
        parcel.writeString(deltaOwn)
        parcel.writeValue(volume)
        parcel.writeString(error)
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