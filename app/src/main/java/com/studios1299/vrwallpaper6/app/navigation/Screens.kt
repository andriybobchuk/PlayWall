package com.studios1299.vrwallpaper6.app.navigation

import kotlinx.serialization.Serializable


sealed class Screens(val route: String?) {

    @Serializable
    object IntroScreen

    @Serializable
    object RegisterScreen

    @Serializable
    object LoginScreen

    @Serializable
    object HomeScreen

    @Serializable
    object ChatScreen

    @Serializable
    object SettingsScreen

    @Serializable
    object ProfileScreen
}
