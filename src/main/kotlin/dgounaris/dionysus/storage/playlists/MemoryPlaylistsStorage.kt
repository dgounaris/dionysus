package dgounaris.dionysus.storage.playlists

import dgounaris.dionysus.playlists.models.Playlist

class MemoryPlaylistsStorage : PlaylistsStorage {
    private val storage: HashMap<String, List<Playlist>> = hashMapOf()

    override fun save(playlists: List<Playlist>, userId: String) {
        storage[userId] = playlists
    }

    override fun getPlaylists(userId: String) =
        storage[userId]
}