package dgounaris.dionysus.clients

import dgounaris.dionysus.clients.models.*

interface SpotifyClient {
    fun getAuthorizeUrl() : String
    fun getTokens(code: String)
    fun refreshToken()
    fun getCurrentUserPlaylists() : CurrentUserPlaylistsResponseDto
    fun getPlaylistTracks(playlistId: String) : PlaylistTracksResponseDto
    suspend fun getTrackAudioAnalysis(trackId: String) : TrackAudioAnalysisResponseDto
    fun playPlaylistTrack(playlistId: String, trackId: String, deviceId: String, positionMs: Int? = null) : String
    fun getTrack(trackId: String): TrackResponseDto
    fun playNext() : String
    fun seekPlaybackPosition(positionMs: Int) : String
    fun getPlaybackState() : GetPlaybackStateResponseDto?
    fun setVolume(volumePercent: Int) : String
    fun getAvailableDevices() : GetAvailableDevicesResponseDto
}