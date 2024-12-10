package com.studios1299.playwall.core.data.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkMonitor {

    private const val TAG = "NetworkMonitor"
    private val _isOnline = MutableStateFlow(false)  // Start with false, assuming no network at launch
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private lateinit var connectivityManager: ConnectivityManager

    fun initialize(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check the current active network state when app starts
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            _isOnline.value = true
            Log.e(TAG, "Initial network status: Online")
        } else {
            _isOnline.value = false
            Log.e(TAG, "Initial network status: Offline")
        }

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Log only when network becomes available
                Log.e(TAG, "Network available")
                _isOnline.value = true
            }

            override fun onLost(network: Network) {
                // Log when the network is lost
                Log.e(TAG, "Network lost")
                _isOnline.value = false
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val hasInternetCapability = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                if (hasInternetCapability) {
                    _isOnline.value = true
                } else {
                    _isOnline.value = false
                }
            }

            override fun onUnavailable() {
                // Log only when network becomes unavailable
                Log.e(TAG, "Network unavailable")
                _isOnline.value = false
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}
