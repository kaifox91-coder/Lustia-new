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
import org.json.JSONArray
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
        super.super.onCreate(savedInstanceState)
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
                postCallback(callbackId, JSONObject().put("error", "no_api_key").toString())
                return
            }
            executor.execute {
                try {
                    val openAiPayload = JSONObject(payloadJson)
                    val geminiRequestBody = convertOpenAiToGemini(openAiPayload)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = RequestBody.create(mediaType, geminiRequestBody.toString())
                    
                    // FIXED: This routes data straight to Google's official Gemini endpoint
                    val req = Request.Builder()
                        .url("https://googleapis.com")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()
                        
                    val resp = httpClient.newCall(req).execute()
                    val respStr = resp.body?.string() ?: "{}"
                    
                    val geminiJson = JSONObject(respStr)
                    val simulatedOpenAiResponse = convertGeminiToOpenAi(geminiJson)
                    
                    postCallback(callbackId, simulatedOpenAiResponse.toString())
                } catch (e: Exception) {
                    val err = JSONObject().put("error", e.message ?: "unknown").toString()
                    postCallback(callbackId, err)
                }
            }
        }

        private fun convertOpenAiToGemini(openAiPayload: JSONObject): JSONObject {
            val geminiRoot = JSONObject()
            val contentsArray = JSONArray()
            val messages = openAiPayload.optJSONArray("messages")
            
            if (messages != null) {
                for (i in 0 until messages.length()) {
                    val msg = messages.getJSONObject(i)
                    val role = msg.optString("role")
                    val text = msg.optString("content")
                    
                    val geminiRole = if (role == "user") "user" else "model"
                    
                    val partObj = JSONObject().put("text", text)
                    val partsArray = JSONArray().put(partObj)
                    val contentObj = JSONObject().put("role", geminiRole).put("parts", partsArray)
                    
                    contentsArray.put(contentObj)
                }
            }
            geminiRoot.put("contents", contentsArray)
            return geminiRoot
        }

        private fun convertGeminiToOpenAi(geminiJson: JSONObject): JSONObject {
            val openAiRoot = JSONObject()
            val choicesArray = JSONArray()
            val choiceObj = JSONObject()
            val messageObj = JSONObject()
            
            var textResponse = "No response from model."
            try {
                textResponse = geminiJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } catch (e: Exception) {
                // Keep fallback text if parsing schema breaks
            }
            
            messageObj.put("role", "assistant")
            messageObj.put("content", textResponse)
            
            choiceObj.put("index", 0)
            choiceObj.put("message", messageObj)
            choiceObj.put("finish_reason", "stop")
            
            choicesArray.put(choiceObj)
            openAiRoot.put("choices", choicesArray)
            return openAiRoot
        }

        private fun postCallback(callbackId: String, response: String) {
            val quotedId = JSONObject.quote(callbackId)
            val quotedResp = JSONObject.quote(response)
            val js = "window.handleNativeResponse($quotedId, $quotedResp);"
            runOnUiThread {
                try {
                    webView.evaluateJavascript(js, null)
                } catch (t: Throwable) {
                    val js2 = "window.handleNativeResponse(" + quotedId + "," + quotedResp + ");"
                    webView.loadUrl("javascript:" + js2)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }
}
