/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.iottest

import android.app.Instrumentation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

import java.lang.System
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    fun assetContents(assetName: String) : String {
        val testContext = InstrumentationRegistry.getInstrumentation().targetContext
        val testRes = testContext.getResources()

        testRes.assets.open(assetName).use {res ->
            val bytes = ByteArray(res.available())
            res.read(bytes)
            return String(bytes).trim()
        }
    }

    @Test
    fun testPass(){
        System.out.print("testPass")
    }

    @Test
    fun testFail(){
        fail("testFail")
    }
}