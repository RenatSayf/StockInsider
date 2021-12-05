package com.renatsayf.stockinsider.models

import android.os.Parcel
import android.os.Parcelable

data class Deal(var filingDate : String?) : Parcelable
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
            val newValue = value?.replace(Regex("^/"), "")
            field = newValue
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
        set(value)
        {
            field = value?.replace("\$", "")
        }
    var qty : String? = ""
        set(value)
        {
            field = value?.replace(",", " ")
        }
    var owned : String? = ""
        set(value)
        {
            field = value?.replace(",", " ")
        }
    var deltaOwn : String? = ""
    var volumeStr : String? = ""
        set(value)
        {
            field = value ?: ""
            if (value != null && value.isNotEmpty())
            {
                volume = try
                {
                    val regex = Regex("""\D""")
                    val strNum = value.replace(regex, "")
                    strNum.toDouble()
                }
                catch (e: Exception)
                {
                    0.0
                }
            }
            else
            {
                volume = 0.0
            }
        }
    var volume : Double = 0.0
        private set

    var error : String? = ""

    var color: Int = 0

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
        qty = parcel.readString()
        owned = parcel.readString()
        deltaOwn = parcel.readString()
        volumeStr = parcel.readString()
        volume = parcel.readDouble()
        error = parcel.readString()
        color = parcel.readInt()
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
        parcel.writeString(qty)
        parcel.writeString(owned)
        parcel.writeString(deltaOwn)
        parcel.writeString(volumeStr)
        parcel.writeDouble(volume)
        parcel.writeString(error)
        parcel.writeInt(color)
    }

    override fun describeContents() : Int
    {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Deal>
    {
        val KEY_DEAL_LIST : String = "${this::class.java.simpleName}.key_deal_list"
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