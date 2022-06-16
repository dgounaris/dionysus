package dgounaris.dionysus.tracks

import org.koin.dsl.module

val tracksModule = module {
    single<TrackDetailsProvider> { TrackDetailsProviderImpl(get(), get()) }
}