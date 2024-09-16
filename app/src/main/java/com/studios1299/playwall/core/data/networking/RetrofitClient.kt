package com.studios1299.playwall.core.data.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://77.237.234.47:3000/"
    val userService: UserService by lazy {
        createRetrofit().create(UserService::class.java)
    }
    val friendsApi: FriendsApi by lazy {
        createRetrofit().create(FriendsApi::class.java)
    }
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}