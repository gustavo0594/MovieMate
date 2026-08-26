package com.gquesada.moviemate

import android.app.Application
import com.gquesada.moviemate.di.databaseModule
import com.gquesada.moviemate.di.domainModule
import com.gquesada.moviemate.di.networkModule
import com.gquesada.moviemate.di.repositoryModule
import com.gquesada.moviemate.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MovieMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MovieMateApp)
            modules(networkModule, databaseModule, repositoryModule, domainModule, viewModelModule)
        }
    }
}
