package dgounaris.dionysus.playlists

import dgounaris.dionysus.playlists.models.Playlist

interface PlaylistDetailsProvider {
    fun getCurrentUserPlaylistNames() : List<String>
    fun getPlaylistDetails(playlistName: String) : Playlist
}