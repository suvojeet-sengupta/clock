package com.suvojeet.clock.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    object Clock : Screen()

    @Serializable
    object Alarm : Screen()

    @Serializable
    object Timer : Screen()

    @Serializable
    object Stopwatch : Screen()

    @Serializable
    object Settings : Screen()

    @Serializable
    object WorldClock : Screen()

    @Serializable
    object AddLocation : Screen()

    @Serializable
    object SleepTimer : Screen()

    @Serializable
    object Setup : Screen()
}
