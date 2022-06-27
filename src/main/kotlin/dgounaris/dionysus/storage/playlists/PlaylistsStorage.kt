package dgounaris.dionysus.storage.playlists

import dgounaris.dionysus.playlists.models.Playlist

interface PlaylistsStorage {
    fun save(playlists: List<Playlist>, userId: String)
    fun getPlaylistsByUserId(userId: String): List<Playlist>?
}