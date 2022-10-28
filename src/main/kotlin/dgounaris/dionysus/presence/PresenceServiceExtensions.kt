package dgounaris.dionysus.presence

import org.koin.dsl.module

val playlistsModule = module {
    single<PresenceHandler> { PresenceHandlerImpl(get()) }
}