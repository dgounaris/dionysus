package dgounaris.dionysus.storage

import dgounaris.dionysus.storage.playlists.MemoryPlaylistsStorage
import dgounaris.dionysus.storage.playlists.PlaylistsStorage
import dgounaris.dionysus.storage.tracks.MemoryTracksStorage
import dgounaris.dionysus.storage.tracks.TracksStorage
import dgounaris.dionysus.storage.user.MemoryUserStorage
import dgounaris.dionysus.storage.user.UserStorage
import org.koin.dsl.module

val storageModule = module {
    single<UserStorage> { MemoryUserStorage() }
    single<PlaylistsStorage> { MemoryPlaylistsStorage() }
    single<TracksStorage> { MemoryTracksStorage() }
}