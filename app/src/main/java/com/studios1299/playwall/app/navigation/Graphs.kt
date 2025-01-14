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
            const val qr_invite = "qr-invite"
            const val diamonds = "diamonds"
            const val lucky_spin = "lucky-spin"
            const val play_chat = "play-chat"
            const val explore = "explore"
            const val explore_image = "explore-image/{photoIndex}"
            const val create = "create"
            const val profile = "profile"
            const val wrzutomat_small = "wrzutomat-small"
            const val wrzutomat_big = "wrzutomat-big"
        }
    }

    object Shared {
        const val root = "shared"
        object Screens {
            const val web = "web/{webType}"
        }
    }
}
