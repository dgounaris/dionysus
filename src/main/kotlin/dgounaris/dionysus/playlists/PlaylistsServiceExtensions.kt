package dgounaris.dionysus.playlists

import org.koin.dsl.module

val playlistsModule = module {
    single<PlaylistDetailsProvider> { PlaylistDetailsProviderImpl(get(), get(), get()) }
}