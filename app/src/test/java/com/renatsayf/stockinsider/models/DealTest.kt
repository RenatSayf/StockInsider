package com.renatsayf.stockinsider.models

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DealTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getVolume() {
        val deal = Deal("").apply {
            volumeStr = "1004"
        }
        val actualVolume = deal.volume
        Assert.assertEquals(1004.0, actualVolume, 0.0)
    }


}