package com.suvojeet.clock.data.alexa

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AlexaApiService {
    @POST("v1/alerts/reminders")
    suspend fun createReminder(
        @Header("Authorization") token: String,
        @Body request: AlexaReminderRequest
    ): Response<Any>
}
