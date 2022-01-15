package dgounaris.dionysus.clients

import org.koin.dsl.module

val clientsModule = module {
    single<SpotifyClient> { SpotifyClientImpl() }
}