package com.dungeonboss.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        webView.loadUrl("file:///android_asset/index.html")
    }

    inner class AndroidBridge {

        @JavascriptInterface
        fun setApiKey(key: String) {
            prefs.edit().putString(KEY_API, key).apply()
        }

        @JavascriptInterface
        fun getApiKey(): String {
            return prefs.getString(KEY_API, "") ?: ""
        }

        @JavascriptInterface
        fun openAIProxy(payloadJson: String, callbackId: String) {
            executor.execute {
                try {
                    val payload = JSONObject(payloadJson)
                    val apiKey = getApiKey()
                    if (apiKey.isEmpty()) {
                        val err = JSONObject()
                        err.put("error", "No API key set")
                        invokeCallback(callbackId, err.toString())
                        return@execute
                    }

                    val req = Request.Builder()
                        .url("https://api.openai.com/v1/chat/completions")
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(payloadJson.toRequestBody("application/json".toMediaType()))
                        .build()

                    val resp = httpClient.newCall(req).execute()
                    val body = resp.body?.string() ?: "{}"
                    val result = JSONObject()
                    result.put("statusCode", resp.code)
                    result.put("body", JSONObject(body))
                    invokeCallback(callbackId, result.toString())
                } catch (e: Exception) {
                    val err = JSONObject()
                    err.put("error", e.message ?: "Unknown error")
                    invokeCallback(callbackId, err.toString())
                }
            }
        }

        @JavascriptInterface
        fun saveSession(sessionId: String, sessionJson: String) {
            prefs.edit().putString("session_$sessionId", sessionJson).apply()
        }

        @JavascriptInterface
        fun loadSession(sessionId: String, callbackId: String) {
            executor.execute {
                val json = prefs.getString("session_$sessionId", null)
                if (json != null) {
                    invokeCallback(callbackId, json)
                } else {
                    val err = JSONObject()
                    err.put("error", "not_found")
                    invokeCallback(callbackId, err.toString())
                }
            }
        }

        private fun invokeCallback(callbackId: String, payload: String) {
            val js = "window.handleNativeResponse('$callbackId', ${JSONObject.quote(payload)})"
            webView.post {
                webView.evaluateJavascript(js, null)
            }
        }
    }
}
