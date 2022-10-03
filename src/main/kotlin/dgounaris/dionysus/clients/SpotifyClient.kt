package dgounaris.dionysus.clients

import dgounaris.dionysus.clients.models.*

interface SpotifyClient {
    fun getAuthorizeUrl() : String
    fun getTokens(code: String)
    fun refreshToken()
    fun getCurrentUser() : String
    fun getUserPlaylists(userId: String) : CurrentUserPlaylistsResponseDto
    fun getPlaylistTracks(playlistId: String) : PlaylistTracksResponseDto
    suspend fun getTrackAudioAnalysis(trackId: String) : TrackAudioAnalysisResponseDto?
    suspend fun getTrackAudioFeatures(trackId: String) : TrackAudioFeaturesResponseDto
    fun playTrack(userId: String, trackId: String, deviceId: String, positionMs: Int? = null) : String
    fun pausePlayback(userId: String): String
    fun getTrack(trackId: String): TrackResponseDto
    fun seekPlaybackPosition(positionMs: Int) : String
    fun getPlaybackState() : GetPlaybackStateResponseDto?
    fun setVolume(volumePercent: Int) : String
    fun getAvailableDevices(userId: String) : GetAvailableDevicesResponseDto
}