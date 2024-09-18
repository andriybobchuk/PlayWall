package com.studios1299.playwall.core.data.remote

import com.studios1299.playwall.BuildConfig
import com.studios1299.playwall.core.data.remote.api.FriendsApi
import com.studios1299.playwall.core.data.remote.api.UserService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val userService: UserService by lazy {
        createRetrofit().create(UserService::class.java)
    }
    val friendsApi: FriendsApi by lazy {
        createRetrofit().create(FriendsApi::class.java)
    }
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}