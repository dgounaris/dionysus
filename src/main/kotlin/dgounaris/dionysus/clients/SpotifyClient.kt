package dgounaris.dionysus.clients

import dgounaris.dionysus.clients.models.*

interface SpotifyClient {
    fun getAuthorizeUrl() : String
    fun authorize(): String
    fun getTokens(code: String)
    fun refreshToken()
    fun getUserPlaylists(userId: String) : CurrentUserPlaylistsResponseDto
    fun getPlaylistTracks(userId: String, playlistId: String) : PlaylistTracksResponseDto
    suspend fun getTrackAudioAnalysis(userId: String, trackId: String) : TrackAudioAnalysisResponseDto?
    suspend fun getTrackAudioFeatures(userId: String, trackId: String) : TrackAudioFeaturesResponseDto
    fun playTrack(userId: String, trackId: String, deviceId: String, positionMs: Int? = null) : String
    fun pausePlayback(userId: String): String
    fun getTrack(userId: String, trackId: String): TrackResponseDto
    fun seekPlaybackPosition(userId: String, positionMs: Int) : String
    fun getPlaybackState(userId: String) : GetPlaybackStateResponseDto?
    fun setVolume(userId: String, volumePercent: Int) : String
    fun getAvailableDevices(userId: String) : GetAvailableDevicesResponseDto
}