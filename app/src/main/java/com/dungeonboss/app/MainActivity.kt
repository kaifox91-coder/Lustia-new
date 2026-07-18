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
import org.json.JSONObject
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

        /**
         * Called from JS: payloadJson (string), callbackId (string)
         * The native side will perform the OpenAI request and call back:
         * window.handleNativeResponse(callbackId, JSON.stringify(response))
         */
        @JavascriptInterface
        fun openAIProxy(payloadJson: String, callbackId: String) {
            val apiKey = prefs.getString(KEY_API, null)
            if (apiKey.isNullOrBlank()) {
                postCallback(callbackId, JSONObject().put("error", "no_api_key").toString())
                return
            }
            executor.execute {
                try {
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = RequestBody.create(mediaType, payloadJson)
                    val req = Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()
                    val resp = httpClient.newCall(req).execute()
                    val respStr = resp.body?.string() ?: "{}"
                    postCallback(callbackId, respStr)
                } catch (e: Exception) {
                    val err = JSONObject().put("error", e.message ?: "unknown").toString()
                    postCallback(callbackId, err)
                }
            }
        }

        private fun postCallback(callbackId: String, response: String) {
            val quotedId = JSONObject.quote(callbackId)
            val quotedResp = JSONObject.quote(response)
            val js = "window.handleNativeResponse($quotedId, $quotedResp);"
            runOnUiThread {
                try {
                    webView.evaluateJavascript(js, null)
                } catch (t: Throwable) {
                    // fallback: loadUrl
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
