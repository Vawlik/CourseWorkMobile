package com.example.newkursach.secondary

import android.content.Context
import android.location.Geocoder
import java.util.*

object Utils {

    // Функция для получения названия города и улицы по координатам
    fun getAddressFromCoordinates(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val cityName = address.locality
                val streetName = address.thoroughfare

                // Возвращаем строку с городом и улицей
                return "$cityName, $streetName"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Если не удалось получить адрес, возвращаем пустую строку или другое значение по умолчанию
        return ""
    }
}