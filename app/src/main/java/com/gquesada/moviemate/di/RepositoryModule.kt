package com.gquesada.moviemate.di

import com.gquesada.moviemate.data.ai.ConversationalPromptEngine
import com.gquesada.moviemate.data.ai.FallbackHeuristicRanker
import com.gquesada.moviemate.data.ai.PromptRecommendationEngine
import com.gquesada.moviemate.data.repository.AssistantRepositoryImpl
import com.gquesada.moviemate.data.repository.MovieRepositoryImpl
import com.gquesada.moviemate.data.repository.RecommendationRepositoryImpl
import com.gquesada.moviemate.data.repository.UserMovieRepositoryImpl
import com.gquesada.moviemate.domain.repository.AssistantRepository
import com.gquesada.moviemate.domain.repository.MovieRepository
import com.gquesada.moviemate.domain.repository.RecommendationRepository
import com.gquesada.moviemate.domain.repository.UserMovieRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { PromptRecommendationEngine() }
    single { FallbackHeuristicRanker() }
    single { ConversationalPromptEngine() }

    single<MovieRepository> { MovieRepositoryImpl(get(), get()) }
    single<UserMovieRepository> { UserMovieRepositoryImpl(get(), get()) }
    single<RecommendationRepository> { RecommendationRepositoryImpl(get(), get(), get()) }
    single<AssistantRepository> { AssistantRepositoryImpl(get()) }
}
