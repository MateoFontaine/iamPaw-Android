package com.example.iampaw.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivity @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** WiFi o datos móviles con acceso a internet (necesario para llamar a Gemini). */
    fun hasInternetForAi(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val onWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val onCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        return hasInternet && (onWifi || onCellular)
    }
}
