package com.renatsayf.stockinsider.models

import android.os.Parcel
import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class DealTest
{

    private lateinit var deal: Deal

    private val inputVolumeStr: String? = "100 v we 4"

    private var expectedVolumeString: String? = null
    private var expectedVolume: Double = 1004.0

    @Before
    fun setUp()
    {
        deal = Deal(Parcel.obtain())
        expectedVolumeString = ""
        deal.volumeStr = inputVolumeStr
    }
    @Test
    fun getVolumeStr()
    {
        val actualVolumeStr = deal.volumeStr.toString()
        val message = "******** expectedVolumeString = $expectedVolumeString  *******  actualVolumeStr = $actualVolumeStr"
        assertEquals(message, expectedVolumeString, actualVolumeStr)
    }

    @Test
    fun getVolume()
    {
        val actualVolume = deal.volume
        val message = "******** expectedVolumeString = $expectedVolume  *******  actualVolumeStr = $actualVolume"
        assertEquals(message, expectedVolume, actualVolume, 0.1)
    }

    @After
    fun tearDown()
    {

    }
}