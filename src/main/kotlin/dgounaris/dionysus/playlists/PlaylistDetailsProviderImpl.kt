package dgounaris.dionysus.playlists

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playlists.models.Playlist
import dgounaris.dionysus.storage.playlists.MemoryPlaylistsStorage
import dgounaris.dionysus.storage.playlists.PlaylistsStorage
import dgounaris.dionysus.tracks.models.Track

class PlaylistDetailsProviderImpl(
    private val spotifyClient: SpotifyClient,
    private val playlistsStorage: PlaylistsStorage,
    private val authorizationController: AuthorizationController
    ) : PlaylistDetailsProvider {
    override fun getCurrentUserPlaylistNames() : List<String> {
        var currentUser = authorizationController.getCurrentUser()
        return spotifyClient.getCurrentUserPlaylists().items.map { playlistItem ->
            playlistItem.name
        }
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