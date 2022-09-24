package dgounaris.dionysus.server

import org.koin.dsl.module

val serverModule = module {
    single<PlaylistsController> { PlaylistsControllerImpl(get(), get(), get(), get()) }
    single<TracksController> { TracksControllerImpl(get(), get()) }
    single<PlaybackController> { PlaybackControllerImpl(get(), get(), get(), get(), get(), get()) }
    single { Server(get(), get(), get(), get()) }
}