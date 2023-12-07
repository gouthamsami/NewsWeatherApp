package com.newsapp

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

//class WebActivity {
//}
class WebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val notfound = findViewById<LinearLayout>(R.id.notfound)
        val webView = findViewById<WebView>(R.id.webView)
        val loading_layout = findViewById<LinearLayout>(R.id.loading_layout)


        val url = intent.getStringExtra("url")
        webView.visibility = View.GONE
        loading_layout.visibility = View.VISIBLE
        Handler().postDelayed(Runnable { /* Create an Intent that will start the Menu-Activity. */
            if (url!!.isEmpty()) {
                webView.visibility = View.GONE
                notfound.visibility = View.VISIBLE
            }else{
                webView.visibility = View.VISIBLE
                loading_layout.visibility = View.GONE
                webView.settings.javaScriptEnabled = true
                webView.webViewClient = WebViewClient()
                webView.loadUrl(url)
            }
        }, 3000)




    }
}