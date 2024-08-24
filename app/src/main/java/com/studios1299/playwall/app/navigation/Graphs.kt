package com.studios1299.playwall.app.navigation

sealed class Graphs {
    object Auth {
        const val root = "auth"
        object Screens {
            const val intro = "intro"
            const val register = "register"
            const val login = "login"
        }
    }

    object Main {
        const val root = "main"
        object Screens {
            const val play = "play"
            const val explore = "explore"
            const val create = "create"
            const val profile = "profile"
        }
    }
}
