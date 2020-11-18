package com.heguro.monst_clip

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity


class LicensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.ui_setting_title_oss_licenses)
        setContentView(R.layout.activity_licenses)
        val webView = findViewById<WebView>(R.id.licensesWebview)
        webView.loadUrl("file:///android_asset/licenses.html")
    }
}
