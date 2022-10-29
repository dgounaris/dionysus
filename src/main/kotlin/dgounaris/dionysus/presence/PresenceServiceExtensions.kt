package dgounaris.dionysus.presence

import org.koin.dsl.module

val presenceModule = module {
    single<PresenceHandler> { PresenceHandlerImpl(get()) }
}