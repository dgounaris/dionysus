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
    suspend fun getTrackAudioAnalysis(trackId: String) : TrackAudioAnalysisResponseDto
    fun playPlaylistTrack(playlistId: String, trackId: String, positionMs: Int? = null) : String
    fun playNext() : String
    fun seekPlaybackPosition(positionMs: Int) : String
}