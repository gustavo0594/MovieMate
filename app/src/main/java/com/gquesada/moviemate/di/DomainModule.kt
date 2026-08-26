package com.gquesada.moviemate.di

import com.gquesada.moviemate.domain.usecase.BuildCandidateSetUseCase
import com.gquesada.moviemate.domain.usecase.GetHomeSectionsUseCase
import com.gquesada.moviemate.domain.usecase.GetSurpriseMeRecommendationUseCase
import org.koin.dsl.module

val domainModule = module {
    single { GetHomeSectionsUseCase(get()) }
    single { BuildCandidateSetUseCase(get(), get()) }
    single { GetSurpriseMeRecommendationUseCase(get(), get()) }
}
