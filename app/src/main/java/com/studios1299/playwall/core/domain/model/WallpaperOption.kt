package com.studios1299.playwall.core.domain.model

enum class WallpaperOption(private val displayName: String) {
    HomeScreen("Home Screen"),
    LockScreen("Lock Screen"),
    Both("Both");

    companion object {
        fun getEnumByDisplayName(name: String): WallpaperOption? {
            return entries.find { it.displayName == name }
        }
        fun getDisplayNames(): List<String> {
            return entries.map { it.displayName }
        }
    }
    override fun toString(): String {
        return displayName
    }
}