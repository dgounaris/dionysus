package dgounaris.dionysus.clients

import dgounaris.dionysus.clients.cache.Cache
import dgounaris.dionysus.clients.cache.PersistentCache
import org.koin.dsl.module

val clientsModule = module {
    single<Cache> { PersistentCache() }
    single<SpotifyClient> { SpotifyClientImpl(get(), get()) }
}