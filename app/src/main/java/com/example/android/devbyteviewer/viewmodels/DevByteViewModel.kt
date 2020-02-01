/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.devbyteviewer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.devbyteviewer.database.getDatabase
import com.example.android.devbyteviewer.domain.DevByteVideo
import com.example.android.devbyteviewer.network.DevByteNetwork
import com.example.android.devbyteviewer.network.asDomainModel
import com.example.android.devbyteviewer.repository.VideosRepository
import kotlinx.coroutines.*
import java.io.IOException

/**
 * DevByteViewModel designed to store and manage UI-related data in a lifecycle conscious way. This
 * allows data to survive configuration changes such as screen rotations. In addition, background
 * work such as fetching network results can continue through configuration changes and deliver
 * results after the new Fragment or Activity is available.
 *
 * @param application The application that this viewmodel is attached to, it's safe to hold a
 * reference to applications across rotation since Application is never recreated during actiivty
 * or fragment lifecycle events.
 */
class DevByteViewModel(application: Application) : AndroidViewModel(application) {


    private val videosRepository = VideosRepository(getDatabase(application))



    /**
     * ViewModel tarafından başlatılan tüm coroutineslerin işini temsil eder.
     *
     * Bu işi iptal etmek. ViewModel tarafından yaratılan tüm coroutine leri iptal edecektir.
     */
    private val viewModelJob = SupervisorJob()

    /**
     *
     * ViewModel tarafından başlatılan tüm coroutines lerin çalışma kapsamını temsil etmektedir.
     *
     * Scope içerisinde oluşturulan işte bulunduğu için scope aracılığıyla cancel() işlemi yapılabilir.
     *
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    /**
     *
     * Uygulamada kullanılacak video listesini LiveData olarak tanımlar. Gözlemlenebilir.
     *
     */
    // private val _playlist = MutableLiveData<List<DevByteVideo>>()

    /**
     * Kapsülleme yapılarak dışarıdan erişimi bu şekilde sağlanacaktır.
     */
    // val playlist: LiveData<List<DevByteVideo>>
       // get() = _playlist

    val playlist = videosRepository.videos

    /**
     * Ağ hatası durumunda tetiklenecek gözlemlenebilir nesnedir.
     * Varsayılan değer olarak false eklenmiştir.
     */
    private var _eventNetworkError = MutableLiveData<Boolean>(false)

    /**
     * Kapsülleme işlemi.
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    /**
     * Hata mesajının görüntülenmesi için gözlemlenebilir nesne. Varsayılan değeri false.
     */
    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    /**
     * Kapsülleme
     */
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    /**
     * ViewModel oluşturulduğunda init {} bloğu içerisinde ki refreshDataFromNetwork() çağırılır
     */
    init {
        // refreshDataFromNetwork()
        refreshDataFromRepository()
    }

    /**
     * Network çağrısı yapılmasını sağlar. İlgili LiveData nesnelerine sonuçları aktarır.
     * ViewModel ın kapsamı içerisinde çalıştırılır. Launch kullanılmaktadır.
     * Burada ki dikkat edilek nokta. Coroutines ' in kapsamı Main Thread 'tir.
     * Fakat API sonuçları await ile alınmktadır. Bir işlem yapılacak ve sonuç alınacağı için await kullanıldı.
     * Retrofit servisi Deferred olarak Call yapmaktadır.
     * Suspend fonksiyon olmamasının sebebi retrofit thread işlemleri yönetmektedir.
     */
    /*private fun refreshDataFromNetwork() = viewModelScope.launch {

        try {
            val playlist = DevByteNetwork.devbytes.getPlaylist().await()
            // Domain Modele manipüle ediliyor.
            _playlist.postValue(playlist.asDomainModel())
            // Hata durumu yok.
            _eventNetworkError.value = false
            _isNetworkErrorShown.value = false

        } catch (networkError: IOException) {
            // Show a Toast error message and hide the progress bar.
            _eventNetworkError.value = true
        }
    }*/

    /**
     * Yeni yöntem ile birlikte video oynatma listesi Repository aracılığıyla getirilir.
     */
    private fun refreshDataFromRepository() {
        viewModelScope.launch {
            try {
                // Yeni sonuçlar eklenir.
                videosRepository.refreshVideos()
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false

            } catch (networkError: IOException) {
                // Show a Toast error message and hide the progress bar.
                if(playlist.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }



    /**
     * Resets the network error flag.
     */
    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }


    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * DevByteViewModel oluşturan Factory Metod.
     * Parametre olarak app almaktadır.
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DevByteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DevByteViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
