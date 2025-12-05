package com.suvojeet.clock.data.alexa

import android.content.Context
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AlexaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val api: AlexaApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.amazonalexa.com/") // Base URL for Alexa API
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        api = retrofit.create(AlexaApiService::class.java)
    }

    suspend fun createReminder(message: String, timeInMillis: Long) {
        val token = AlexaAuthManager.getToken(context)
        if (token == null) {
            Log.e("AlexaRepository", "No token found")
            return
        }

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val scheduledTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timeInMillis), ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val request = AlexaReminderRequest(
            requestTime = now,
            trigger = Trigger(scheduledTime = scheduledTime),
            alertInfo = AlertInfo(
                spokenInfo = SpokenInfo(
                    content = listOf(Content(text = message))
                )
            ),
            pushNotification = PushNotification()
        )

        try {
            val response = api.createReminder("Bearer $token", request)
            if (response.isSuccessful) {
                Log.d("AlexaRepository", "Reminder created successfully")
            } else {
                Log.e("AlexaRepository", "Failed to create reminder: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("AlexaRepository", "Error creating reminder", e)
        }
    }
}
