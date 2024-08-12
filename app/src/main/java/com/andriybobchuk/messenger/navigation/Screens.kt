package com.andriybobchuk.messenger.navigation

import kotlinx.serialization.Serializable


sealed class Screens(val route: String?) {
    @Serializable
    object HomeScreen

    @Serializable
    object ChatScreen

    @Serializable
    object SettingsScreen

    @Serializable
    object ProfileScreen
}
