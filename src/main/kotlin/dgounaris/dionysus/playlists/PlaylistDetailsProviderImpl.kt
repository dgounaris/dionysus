package dgounaris.dionysus.playlists

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playlists.models.Playlist
import dgounaris.dionysus.tracks.models.Track

class PlaylistDetailsProviderImpl(
    private val spotifyClient: SpotifyClient
    ) : PlaylistDetailsProvider {
    override fun getUserPlaylistNames(userId: String) : List<String> {
        return spotifyClient.getUserPlaylists(userId).items.map { playlistItem ->
            playlistItem.name
        }
    }

    override fun getPlaylistDetails(userId: String, playlistName: String) : Playlist {
        val currentUserPlaylists = spotifyClient.getUserPlaylists(userId)
        val selectedPlaylist = currentUserPlaylists.items.first { playlistItem ->
            playlistItem.name == playlistName
        }
        val playlistTracks = spotifyClient.getPlaylistTracks(userId, selectedPlaylist.id)
        return Playlist(
            selectedPlaylist.name,
            selectedPlaylist.id,
            playlistTracks.items.map { track -> Track(track.track.name, track.track.id) }
        )
    }
}