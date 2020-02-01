package com.example.android.devbyteviewer.domain

import com.example.android.devbyteviewer.util.smartTruncate

/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 * @see database Database için kullanılabilir.(Manipülasyon)
 * @see network Network çağrıları için kullanılabilir.(Manipülasyon)
 */

/**
 * Uygulama içerisinde kullanılacak veri modelini temsil eder.
 * DevByteVideo Veri Sınıfı 1 Adet Video yu temsil etmektedir.
 */
data class DevByteVideo(val title: String,
                        val description: String,
                        val url: String,
                        val updated: String,
                        val thumbnail: String) {

    /**
     * Video açıklamasını manipüle etmek için kullanılmaktadır.
     * Karakter sınırını 200 yapmaktadır. Kullanılan metod smartTruncate ' tir.
     */
    val shortDescription: String
        get() = description.smartTruncate(200)
}