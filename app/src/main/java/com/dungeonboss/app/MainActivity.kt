package com.dungeonboss.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences
    private val httpClient = OkHttpClient()
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        private const val PREFS_NAME = "dungeon_prefs"
        private const val KEY_API = "openai_api_key"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")
        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class AndroidBridge(private val ctx: Context) {

        @JavascriptInterface
        fun setApiKey(key: String) {
            prefs.edit().putString(KEY_API, key).apply()
        }

        @JavascriptInterface
        fun getApiKey(): String? {
            return prefs.getString(KEY_API, null)
        }

        @JavascriptInterface
        fun openAIProxy(payloadJson: String, callbackId: String) {
            val apiKey = prefs.getString(KEY_API, null)
            if (apiKey.isNullOrBlank()) {
                val errJson = "{\"error\": \"no_api_key\"}"
                postCallback(callbackId, errJson)
                return
            }
            executor.execute {
                try {
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    // Forwarding the raw Gemini JSON string payload unmodified 
                    val body = RequestBody.create(mediaType, payloadJson)
                    
                    val req = Request.Builder()
                        .url("https://googleapis.com")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()
                        
                    val resp = httpClient.newCall(req).execute()
                    val respStr = resp.body?.string() ?: "{}"
                    
                    // Hand back the raw Gemini API response directly to the javascript parser
                    postCallback(callbackId, respStr)
                } catch (e: Exception) {
                    val errMsg = e.message ?: "unknown"
                    val errJson = "{\"error\": \"$errMsg\"}"
                    postCallback(callbackId, errJson)
                }
            }
        }

        private fun postCallback(callbackId: String, response: String) {
            // Using standard string interpolation to construct clean JS invocation blocks safely
            val js = "window.handleNativeResponse('$callbackId', `$response`);"
            runOnUiThread {
                try {
                    webView.evaluateJavascript(js, null)
                } catch (t: Throwable) {
                    webView.loadUrl("javascript:$js")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }
}
