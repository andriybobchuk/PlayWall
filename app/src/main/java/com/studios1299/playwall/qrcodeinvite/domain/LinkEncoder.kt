package com.studios1299.playwall.qrcodeinvite.domain

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun encodeFullUrl(url: String): String {
    return URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
}

fun decodeUrl(url: String): String {
    return URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
}