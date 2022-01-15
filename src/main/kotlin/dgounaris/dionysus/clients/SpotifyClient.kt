package dgounaris.dionysus.clients

import dgounaris.dionysus.clients.models.CurrentUserPlaylistsResponseDto
import dgounaris.dionysus.clients.models.PlaylistTracksResponseDto
import dgounaris.dionysus.clients.models.TrackAudioAnalysisResponseDto

interface SpotifyClient {
    fun getAuthorizeUrl() : String
    fun getTokens(code: String)
    fun refreshToken()
    fun getCurrentUserPlaylists() : CurrentUserPlaylistsResponseDto
    fun getPlaylistTracks(playlistId: String) : PlaylistTracksResponseDto
    fun getTrackAudioAnalysis(trackId: String) : TrackAudioAnalysisResponseDto
    fun playNext() : String
    fun seekPlaybackPosition(positionMs: Int) : String
}