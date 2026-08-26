package com.gquesada.moviemate.di

import com.gquesada.moviemate.domain.usecase.BuildCandidateSetUseCase
import com.gquesada.moviemate.domain.usecase.GetHomeSectionsUseCase
import com.gquesada.moviemate.domain.usecase.GetPickedForYouUseCase
import com.gquesada.moviemate.domain.usecase.GetSurpriseMeRecommendationUseCase
import com.gquesada.moviemate.domain.usecase.MarkRecommendationAcceptedUseCase
import com.gquesada.moviemate.domain.usecase.ObserveModelAvailabilityUseCase
import org.koin.dsl.module

val domainModule = module {
    single { BuildCandidateSetUseCase(get(), get()) }
    single { GetPickedForYouUseCase(get(), get()) }
    single { GetHomeSectionsUseCase(get(), get()) }
    single { GetSurpriseMeRecommendationUseCase(get(), get()) }
    single { MarkRecommendationAcceptedUseCase(get()) }
    single { ObserveModelAvailabilityUseCase(get()) }
}
