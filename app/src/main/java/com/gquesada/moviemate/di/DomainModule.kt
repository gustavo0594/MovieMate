package com.gquesada.moviemate.di

import com.gquesada.moviemate.domain.usecase.BuildCandidateSetUseCase
import com.gquesada.moviemate.domain.usecase.GetHomeSectionsUseCase
import com.gquesada.moviemate.domain.usecase.GetSurpriseMeRecommendationUseCase
import com.gquesada.moviemate.domain.usecase.GetTasteProfileUseCase
import com.gquesada.moviemate.domain.usecase.MarkRecommendationAcceptedUseCase
import com.gquesada.moviemate.domain.usecase.ObserveAssistantModelAvailabilityUseCase
import com.gquesada.moviemate.domain.usecase.ObserveModelAvailabilityUseCase
import com.gquesada.moviemate.domain.usecase.SendAssistantMessageUseCase
import org.koin.dsl.module

val domainModule = module {
    single { BuildCandidateSetUseCase(get(), get()) }
    single { GetHomeSectionsUseCase(get(), get(), get()) }
    single { GetSurpriseMeRecommendationUseCase(get(), get()) }
    single { GetTasteProfileUseCase(get()) }
    single { MarkRecommendationAcceptedUseCase(get()) }
    single { ObserveModelAvailabilityUseCase(get()) }
    single { SendAssistantMessageUseCase(get(), get()) }
    single { ObserveAssistantModelAvailabilityUseCase(get()) }
}
