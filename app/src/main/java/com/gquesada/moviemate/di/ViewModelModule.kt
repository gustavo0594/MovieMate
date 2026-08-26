package com.gquesada.moviemate.di

import com.gquesada.moviemate.presentation.favorites.FavoritesViewModel
import com.gquesada.moviemate.presentation.favorites.WatchedViewModel
import com.gquesada.moviemate.presentation.favorites.WatchlistViewModel
import com.gquesada.moviemate.presentation.home.HomeViewModel
import com.gquesada.moviemate.presentation.moviedetail.MovieDetailViewModel
import com.gquesada.moviemate.presentation.recommendation.SurpriseMeViewModel
import com.gquesada.moviemate.presentation.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { (movieId: Int) -> MovieDetailViewModel(movieId, get(), get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { WatchedViewModel(get()) }
    viewModel { WatchlistViewModel(get()) }
    viewModel { SurpriseMeViewModel(get(), get()) }
}
