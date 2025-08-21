package com.guicarneirodev.agrotask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guicarneirodev.agrotask.domain.network.NetworkObserver
import com.guicarneirodev.agrotask.domain.network.NetworkStatus
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val networkObserver: NetworkObserver
) : ViewModel() {

    private val _weather = MutableStateFlow<Weather?>(null)
    val weather: StateFlow<Weather?> = _weather.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _networkStatus = MutableStateFlow(NetworkStatus.Available)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    init {
        loadWeather(forceRefresh = false)
        observeWeatherCache()
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkObserver.observe().collect { status ->
                _networkStatus.value = status

                when (status) {
                    NetworkStatus.Available -> {
                        if (_weather.value?.isFromCache == true) {
                            loadWeather(forceRefresh = true)
                        }
                    }
                    NetworkStatus.Lost, NetworkStatus.Unavailable -> {
                        if (_weather.value != null) {
                            _weather.value = _weather.value?.copy(isFromCache = true)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeWeatherCache() {
        viewModelScope.launch {
            weatherRepository.getWeatherFlow().collect { cachedWeather ->
                if (cachedWeather != null && _weather.value == null) {
                    _weather.value = cachedWeather
                }
            }
        }
    }

    fun loadWeather(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val isOnline = _networkStatus.value == NetworkStatus.Available

            try {
                val weatherData = if (isOnline || forceRefresh) {
                    weatherRepository.getCurrentWeather(forceRefresh)
                } else {
                    weatherRepository.getCurrentWeather(false)
                }
                _weather.value = weatherData.copy(
                    isFromCache = !isOnline || weatherData.isFromCache
                )
            } catch (e: Exception) {
                if (!isOnline) {
                    _error.value = "Sem conexão. Mostrando última atualização disponível."
                } else {
                    _error.value = "Erro ao carregar dados do clima."
                }
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshWeather() {
        if (_networkStatus.value == NetworkStatus.Available) {
            loadWeather(forceRefresh = true)
        } else {
            _error.value = "Sem conexão para atualizar"
        }
    }
}