package com.dungeonboss.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var prefs: SharedPreferences
    private val scope = CoroutineScope(Dispatchers.Main)

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
        webView.settings.domStorageEnabled = true
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
                postCallback(callbackId, "{\"error\": \"Missing API Key definition.\"}")
                return
            }

            scope.launch(Dispatchers.IO) {
                try {
                    // Extract the clean prompt text from the incoming JSON packet
                    val json = JSONObject(payloadJson)
                    val promptText = json.getString("prompt")

                    // Instantiate the official Gemini Model directly using Google's SDK libraries
                    val geminiModel = GenerativeModel(
                        modelName = "gemini-2.5-flash",
                        apiKey = apiKey
                    )

                    // Execute content generation task securely
                    val response = geminiModel.generateContent(promptText)
                    val responseText = response.text ?: "No text generated."

                    // Package into a clean JSON reply to pass back to the index interface layout
                    val successJson = JSONObject()
                    successJson.put("text", responseText)
                    
                    postCallback(callbackId, successJson.toString())
                } catch (e: Exception) {
                    val errJson = JSONObject()
                    errJson.put("error", e.message ?: "Unknown Exception.")
                    postCallback(callbackId, errJson.toString())
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
                    webView.loadUrl("javascript:" + js)
                }
            }
        }
    }
}
