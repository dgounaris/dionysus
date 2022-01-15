package dgounaris.dionysus.playlists

import dgounaris.dionysus.playlists.models.Playlist

interface PlaylistDetailsProvider {
    fun getPlaylistDetails(playlistName: String) : Playlist
}