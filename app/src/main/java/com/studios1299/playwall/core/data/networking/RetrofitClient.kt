package com.studios1299.playwall.core.data.networking

import com.studios1299.playwall.BuildConfig
import com.studios1299.playwall.core.data.networking.api.FriendsApi
import com.studios1299.playwall.core.data.networking.api.UserApi
import com.studios1299.playwall.core.data.networking.api.UserAppDataApi
import com.studios1299.playwall.core.data.networking.api.WallpaperApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val userApi: UserApi by lazy {
        createRetrofit().create(UserApi::class.java)
    }
    val userAppDataApi: UserAppDataApi by lazy {
        createRetrofit().create(UserAppDataApi::class.java)
    }
    val friendsApi: FriendsApi by lazy {
        createRetrofit().create(FriendsApi::class.java)
    }
    val wallpaperApi: WallpaperApi by lazy {
        createRetrofit().create(WallpaperApi::class.java)
    }
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}