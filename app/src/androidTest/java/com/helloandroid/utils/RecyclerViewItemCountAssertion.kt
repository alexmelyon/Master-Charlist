package com.helloandroid.utils

import android.support.v7.widget.RecyclerView
import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.ViewAssertion
import android.view.View
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat


class RecyclerViewItemCountAssertion(private val mExpectedCount: Int) : ViewAssertion {

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {

        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        assertThat(adapter.itemCount, `is`(mExpectedCount))
    }
}