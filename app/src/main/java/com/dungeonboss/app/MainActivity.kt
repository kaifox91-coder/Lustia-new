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
}        webView = findViewById(R.id.webView)

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
         * Native converts OpenAI-style payload -> Gemini request,
         * calls Gemini, converts response back to OpenAI shape,
         * and calls window.handleNativeResponse(callbackId, response).
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
                    val openAiPayload = JSONObject(payloadJson)
                    val geminiRequestBody = convertOpenAiToGemini(openAiPayload)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = RequestBody.create(mediaType, geminiRequestBody.toString())

                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "{}"

                    val openAiResponse = convertGeminiToOpenAi(JSONObject(responseBody))

                    postCallback(callbackId, openAiResponse.toString())
                } catch (e: Exception) {
                    val err = JSONObject().put("error", e.message ?: "unknown").toString()
                    postCallback(callbackId, err)
                }
            }
        }

        private fun convertOpenAiToGemini(openAi: JSONObject): JSONObject {
            val contents = JSONArray()
            val systemParts = JSONArray()

            val messages = openAi.optJSONArray("messages") ?: JSONArray()
            for (i in 0 until messages.length()) {
                val msg = messages.getJSONObject(i)
                val role = msg.optString("role", "user")
                val content = msg.optString("content", "")

                if (role == "system") {
                    systemParts.put(JSONObject().put("text", content))
                } else {
                    val geminiRole = if (role == "assistant") "model" else "user"
                    val part = JSONObject().put("text", content)
                    val msgObj = JSONObject().put("role", geminiRole).put("parts", JSONArray().put(part))
                    contents.put(msgObj)
                }
            }

            val gemini = JSONObject()
            gemini.put("contents", contents)
            if (systemParts.length() > 0) {
                gemini.put("system_instruction", JSONObject().put("parts", systemParts))
            }

            val generationConfig = JSONObject()
            if (openAi.has("temperature"))
                generationConfig.put("temperature", openAi.getDouble("temperature"))
            if (openAi.has("top_p"))
                generationConfig.put("topP", openAi.getDouble("top_p"))
            if (openAi.has("max_tokens"))
                generationConfig.put("maxOutputTokens", openAi.getInt("max_tokens"))
            if (generationConfig.length() > 0)
                gemini.put("generationConfig", generationConfig)

            return gemini
        }

        private fun convertGeminiToOpenAi(geminiResp: JSONObject): JSONObject {
            val openAiResp = JSONObject()
            val choices = JSONArray()

            val candidates = geminiResp.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    var text = ""
                    if (parts != null && parts.length() > 0) {
                        val firstPart = parts.getJSONObject(0)
                        text = firstPart.optString("text", "")
                    }
                    val message = JSONObject().put("role", "assistant").put("content", text)
                    val choice = JSONObject().put("message", message)
                    choices.put(choice)
                }
            }

            val promptFeedback = geminiResp.optJSONObject("promptFeedback")
            if (promptFeedback != null) {
                val blockReason = promptFeedback.optString("blockReason", "")
                if (blockReason.isNotEmpty()) {
                    openAiResp.put("error", "Blocked: $blockReason")
                }
            }

            openAiResp.put("choices", choices)
            return openAiResp
        }

        private fun postCallback(callbackId: String, response: String) {
            val quotedId = JSONObject.quote(callbackId)
            val quotedResp = JSONObject.quote(response)
            val js = "window.handleNativeResponse($quotedId, $quotedResp);"
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
}        webView = findViewById(R.id.webView)

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
         * Called from JS with the same OpenAI‑style payload:
         * { model: "gpt-4...", messages: [...], temperature: ... }
         *
         * This method converts it to Gemini format, calls the API,
         * then converts the Gemini response back to OpenAI shape.
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
                    // ---- Convert OpenAI payload to Gemini request ----
                    val openAiPayload = JSONObject(payloadJson)
                    val geminiRequestBody = convertOpenAiToGemini(openAiPayload)

                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    val body = RequestBody.create(mediaType, geminiRequestBody.toString())

                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body?.string() ?: "{}"

                    // ---- Convert Gemini response back to OpenAI format ----
                    val openAiResponse = convertGeminiToOpenAi(JSONObject(responseBody))

                    postCallback(callbackId, openAiResponse.toString())
                } catch (e: Exception) {
                    val err = JSONObject().put("error", e.message ?: "unknown").toString()
                    postCallback(callbackId, err)
                }
            }
        }

        /**
         * Transforms an OpenAI‑style request to Gemini's format.
         * Model is ignored – we always use gemini-2.0-flash (free tier).
         * System message is moved to system_instruction.
         */
        private fun convertOpenAiToGemini(openAi: JSONObject): JSONObject {
            val contents = JSONArray()
            val systemParts = JSONArray()

            val messages = openAi.optJSONArray("messages") ?: JSONArray()
            for (i in 0 until messages.length()) {
                val msg = messages.getJSONObject(i)
                val role = msg.optString("role", "user")
                val content = msg.optString("content", "")

                if (role == "system") {
                    systemParts.put(JSONObject().put("text", content))
                } else {
                    // Gemini expects "user" or "model" roles
                    val geminiRole = if (role == "assistant") "model" else "user"
                    val part = JSONObject().put("text", content)
                    val msgObj = JSONObject().put("role", geminiRole).put("parts", JSONArray().put(part))
                    contents.put(msgObj)
                }
            }

            val gemini = JSONObject()
            gemini.put("contents", contents)
            if (systemParts.length() > 0) {
                gemini.put("system_instruction", JSONObject().put("parts", systemParts))
            }

            // Copy temperature, top_p, maxOutputTokens if provided
            val generationConfig = JSONObject()
            if (openAi.has("temperature"))
                generationConfig.put("temperature", openAi.getDouble("temperature"))
            if (openAi.has("top_p"))
                generationConfig.put("topP", openAi.getDouble("top_p"))
            if (openAi.has("max_tokens"))
                generationConfig.put("maxOutputTokens", openAi.getInt("max_tokens"))
            if (generationConfig.length() > 0)
                gemini.put("generationConfig", generationConfig)

            return gemini
        }

        /**
         * Converts Gemini response to the structure your JS expects:
         * { choices: [ { message: { content: "..." } } ] }
         */
        private fun convertGeminiToOpenAi(geminiResp: JSONObject): JSONObject {
            val openAiResp = JSONObject()
            val choices = JSONArray()

            val candidates = geminiResp.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    var text = ""
                    if (parts != null && parts.length() > 0) {
                        val firstPart = parts.getJSONObject(0)
                        text = firstPart.optString("text", "")
                    }
                    val message = JSONObject().put("role", "assistant").put("content", text)
                    val choice = JSONObject().put("message", message)
                    choices.put(choice)
                }
            }

            // If Gemini returns a safety block, show a helpful error
            val promptFeedback = geminiResp.optJSONObject("promptFeedback")
            if (promptFeedback != null) {
                val blockReason = promptFeedback.optString("blockReason", "")
                if (blockReason.isNotEmpty()) {
                    val errorMsg = "Blocked: $blockReason"
                    openAiResp.put("error", errorMsg)
                }
            }

            openAiResp.put("choices", choices)
            // Also add usage info if needed (Gemini doesn't provide token counts)
            return openAiResp
        }

        private fun postCallback(callbackId: String, response: String) {
            val quotedId = JSONObject.quote(callbackId)
            val quotedResp = JSONObject.quote(response)
            val js = "window.handleNativeResponse($quotedId, $quotedResp);"
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
