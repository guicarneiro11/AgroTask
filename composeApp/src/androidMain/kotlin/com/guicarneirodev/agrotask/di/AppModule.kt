package com.guicarneirodev.agrotask.di

import androidx.room.Room
import com.guicarneirodev.agrotask.BuildConfig
import com.guicarneirodev.agrotask.data.firebase.FirebaseService
import com.guicarneirodev.agrotask.data.local.database.AppDatabase
import com.guicarneirodev.agrotask.data.location.LocationService
import com.guicarneirodev.agrotask.data.network.AndroidNetworkObserver
import com.guicarneirodev.agrotask.data.permissions.AndroidPermissionHandler
import com.guicarneirodev.agrotask.data.remote.WeatherApiService
import com.guicarneirodev.agrotask.data.repository.ActivityRepositoryImpl
import com.guicarneirodev.agrotask.data.repository.TaskRepositoryImpl
import com.guicarneirodev.agrotask.data.repository.WeatherRepositoryImpl
import com.guicarneirodev.agrotask.domain.network.NetworkObserver
import com.guicarneirodev.agrotask.domain.sync.SyncManager
import com.guicarneirodev.agrotask.domain.permissions.PermissionHandler
import com.guicarneirodev.agrotask.domain.repository.ActivityRepository
import com.guicarneirodev.agrotask.domain.repository.TaskRepository
import com.guicarneirodev.agrotask.domain.repository.WeatherRepository
import com.guicarneirodev.agrotask.presentation.viewmodel.ActivityViewModel
import com.guicarneirodev.agrotask.presentation.viewmodel.TaskViewModel
import com.guicarneirodev.agrotask.presentation.viewmodel.WeatherViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.*

val appModule = module {

    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "agrotask_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().taskDao() }
    single { get<AppDatabase>().activityRecordDao() }
    single { get<AppDatabase>().weatherCacheDao() }

    single { FirebaseService() }
    single { WeatherApiService() }
    single { LocationService(androidContext()) }
    single<NetworkObserver> { AndroidNetworkObserver(androidContext()) }
    single<PermissionHandler> { AndroidPermissionHandler(androidContext()) }

    single<TaskRepository> {
        TaskRepositoryImpl(
            taskDao = get(),
            firebaseService = get()
        )
    }

    single<ActivityRepository> {
        ActivityRepositoryImpl(
            activityRecordDao = get(),
            firebaseService = get()
        )
    }

    single<WeatherRepository> {
        WeatherRepositoryImpl(
            weatherApiService = get(),
            weatherCacheDao = get(),
            locationService = get(),
            apiKey = BuildConfig.WEATHER_API_KEY
        )
    }

    single {
        SyncManager(
            taskRepository = get(),
            activityRepository = get(),
            networkObserver = get()
        )
    }

    viewModel {
        TaskViewModel(
            taskRepository = get(),
            syncManager = get()
        )
    }

    viewModel {
        ActivityViewModel(
            activityRepository = get(),
            syncManager = get()
        )
    }

    viewModel {
        WeatherViewModel(
            weatherRepository = get(),
            networkObserver = get()
        )
    }
}