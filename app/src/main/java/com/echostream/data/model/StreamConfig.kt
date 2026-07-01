package com.echostream.data.model

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class StreamConfig(
    val invidiousInstances: List<String> = DEFAULT_INVIDIOUS,
    val pipedInstances: List<String> = DEFAULT_PIPED,
    val youtubeMusicKey: String = DEFAULT_YT_MUSIC_KEY,
) {
    companion object {
        private const val TAG = "StreamConfig"
        private const val CONFIG_URL =
            "https://raw.githubusercontent.com/Aeroflash-r1/ApexWear-App/main/stream_config.json"

        val DEFAULT_INVIDIOUS = listOf(
            "https://inv.tux.pizza",
            "https://invidious.private.coffee",
            "https://invidious.projectsegfau.lt",
            "https://yt.artemislena.eu",
            "https://invidious.slipfox.xyz",
            "https://invidious.fdn.fr",
            "https://vid.puffyan.us",
            "https://invidious.nerdvpn.de",
        )

        val DEFAULT_PIPED = listOf(
            "https://pipedapi.kavin.rocks",
            "https://piped-api.noseka1.com",
            "https://pipedapi.drgns.space",
            "https://api.piped.yt",
            "https://pipedapi.r4fo.com",
        )

        const val DEFAULT_YT_MUSIC_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"

        private val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        suspend fun loadFromRemote(): StreamConfig {
            return try {
                val request = Request.Builder()
                    .url(CONFIG_URL)
                    .header("User-Agent", "EchoStream/1.0")
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.w(TAG, "Config fetch returned HTTP ${response.code}, using defaults")
                        return@use StreamConfig()
                    }
                    val body = response.body?.string() ?: return@use StreamConfig()
                    parse(body)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load remote config, using defaults", e)
                StreamConfig()
            }
        }

        private fun parse(json: String): StreamConfig {
            val root = JSONObject(json)
            return StreamConfig(
                invidiousInstances = root.optJSONArray("invidiousInstances")
                    ?.let { arr ->
                        (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
                    }?.takeIf { it.isNotEmpty() } ?: DEFAULT_INVIDIOUS,
                pipedInstances = root.optJSONArray("pipedInstances")
                    ?.let { arr ->
                        (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }
                    }?.takeIf { it.isNotEmpty() } ?: DEFAULT_PIPED,
                youtubeMusicKey = root.optString("youtubeMusicKey")
                    .takeIf { it.isNotBlank() } ?: DEFAULT_YT_MUSIC_KEY,
            )
        }
    }
}
