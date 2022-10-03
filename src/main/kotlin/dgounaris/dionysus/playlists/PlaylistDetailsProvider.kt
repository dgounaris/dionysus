package dgounaris.dionysus.playlists

import dgounaris.dionysus.playlists.models.Playlist

interface PlaylistDetailsProvider {
    fun getUserPlaylistNames(userId: String) : List<String>
    fun getPlaylistDetails(userId: String, playlistName: String) : Playlist
}