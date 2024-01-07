package com.example.newkursach.secondary

import android.content.Context
import android.location.Geocoder
import java.util.Locale

const val MICROPHONE_REQUEST_CODE = 200
const val PERMISSIONS_REQUEST_LOCATION = 123

object Utils {

    fun getAddressFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val cityName = address.locality
                val streetName = address.thoroughfare

                return "$cityName, $streetName"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}