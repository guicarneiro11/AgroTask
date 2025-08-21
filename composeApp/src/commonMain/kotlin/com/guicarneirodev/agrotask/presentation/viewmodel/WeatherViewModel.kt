package com.guicarneirodev.agrotask.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guicarneirodev.agrotask.domain.model.Weather
import com.guicarneirodev.agrotask.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _weather = MutableStateFlow<Weather?>(null)
    val weather: StateFlow<Weather?> = _weather.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadWeather(forceRefresh = false)
        observeWeatherCache()
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
            try {
                val weatherData = weatherRepository.getCurrentWeather(forceRefresh)
                _weather.value = weatherData
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados do clima. Mostrando última atualização."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshWeather() {
        loadWeather(forceRefresh = true)
    }
}