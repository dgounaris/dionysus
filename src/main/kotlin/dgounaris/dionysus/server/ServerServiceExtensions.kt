package dgounaris.dionysus.server

import org.koin.dsl.module

val serverModule = module {
    single<PlaylistsController> { PlaylistsControllerImpl(get(), get(), get()) }
    single<PlaybackController> { PlaybackControllerImpl(get(), get(), get()) }
    single<PlaybackPlanController> { PlaybackPlanControllerImpl(get(), get(), get(), get(), get()) }
    single<StatePollingController> { StatePollingControllerImpl(get(), get(), get()) }
    single { Server(get(), get(), get(), get(), get()) }
}