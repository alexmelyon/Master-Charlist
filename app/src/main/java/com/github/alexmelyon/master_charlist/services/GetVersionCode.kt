package com.github.alexmelyon.master_charlist.services

import android.content.Context
import android.os.AsyncTask
import org.jsoup.Jsoup

class GetVersionCode(private val context: Context) : AsyncTask<Void?, String?, String?>() {

    var onPost: ((String?) -> Unit)? = null

    override fun doInBackground(vararg params: Void?): String? {
        var newVersion: String? = null
        return try {
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + context.packageName + "&hl=it")
                .timeout(30000)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .get()
                .select("div[itemprop=softwareVersion]")
                .first()
                .ownText()
            newVersion
        } catch (e: Exception) {
            newVersion
        }
    }

    override fun onPostExecute(result: String?) {
        onPost?.invoke(result)
    }
}