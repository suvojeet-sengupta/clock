package com.suvojeet.clock.data.alexa

import com.google.gson.annotations.SerializedName

data class AlexaReminderRequest(
    @SerializedName("requestTime") val requestTime: String,
    @SerializedName("trigger") val trigger: Trigger,
    @SerializedName("alertInfo") val alertInfo: AlertInfo,
    @SerializedName("pushNotification") val pushNotification: PushNotification
)

data class Trigger(
    @SerializedName("type") val type: String = "SCHEDULED_ABSOLUTE",
    @SerializedName("scheduledTime") val scheduledTime: String,
    @SerializedName("timeZoneId") val timeZoneId: String = java.util.TimeZone.getDefault().id
)

data class AlertInfo(
    @SerializedName("spokenInfo") val spokenInfo: SpokenInfo
)

data class SpokenInfo(
    @SerializedName("content") val content: List<Content>
)

data class Content(
    @SerializedName("locale") val locale: String = "en-US",
    @SerializedName("text") val text: String
)

data class PushNotification(
    @SerializedName("status") val status: String = "ENABLED"
)
