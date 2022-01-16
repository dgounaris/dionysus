package dgounaris.dionysus.playlists

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playlists.models.Playlist
import dgounaris.dionysus.tracks.models.Track

class PlaylistDetailsProviderImpl(private val spotifyClient: SpotifyClient) : PlaylistDetailsProvider {
    override fun getCurrentUserPlaylistNames() =
        spotifyClient.getCurrentUserPlaylists().items.map { playlistItem ->
            playlistItem.name
        }

    override fun getPlaylistDetails(playlistName: String) : Playlist {
        val currentUserPlaylists = spotifyClient.getCurrentUserPlaylists()
        val selectedPlaylist = currentUserPlaylists.items.first { playlistItem ->
            playlistItem.name == playlistName
        }
        val playlistTracks = spotifyClient.getPlaylistTracks(selectedPlaylist.id)
        return Playlist(
            selectedPlaylist.name,
            selectedPlaylist.id,
            playlistTracks.items.map { track -> Track(track.track.name, track.track.id) }
        )
    }
}