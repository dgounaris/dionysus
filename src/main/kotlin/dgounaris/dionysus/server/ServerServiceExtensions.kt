package dgounaris.dionysus.server

import org.koin.dsl.module

val serverModule = module {
    single<AuthorizationController> { AuthorizationControllerImpl(get()) }
    single<PlaylistsController> { PlaylistsControllerImpl(get(), get()) }
    single<TracksController> { TracksControllerImpl(get()) }
    single<PlaybackController> { PlaybackControllerImpl(get(), get()) }
    single { Server(get(), get(), get(), get()) }
}