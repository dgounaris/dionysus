package dgounaris.dionysus.dionysense

import org.koin.dsl.module

val dionysenseModule = module {
    single<FeedbackHandler> { FeedbackHandlerImpl(get(), get()) }
    single<TrackSectionSelector> { TrackSectionSelectorImpl(get()) }
    single<TrackOrderSelector> { TrackOrderSelectorImpl(get()) }
}