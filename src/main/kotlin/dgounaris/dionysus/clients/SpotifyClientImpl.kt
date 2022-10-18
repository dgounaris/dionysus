package dgounaris.dionysus.clients

import com.fasterxml.jackson.databind.DeserializationFeature
import dgounaris.dionysus.clients.cache.Cache
import dgounaris.dionysus.clients.models.*
import dgounaris.dionysus.common.PropertiesProvider
import dgounaris.dionysus.storage.user.UserStorage
import dgounaris.dionysus.user.models.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.observer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import java.util.*

class SpotifyClientImpl(
    private val cache: Cache,
    private val userStorage: UserStorage
) : SpotifyClient {
    private val clientId = PropertiesProvider.configuration.getProperty("spotifyClientId")
    private val clientSecret = PropertiesProvider.configuration.getProperty("spotifyClientSecret")
    private val httpClient = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            jackson {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            exponentialDelay()
        }
        ResponseObserver { response ->
            println("Spotify client: ${response.call.request.url} - ${response.status.value}")
        }
    }

    override fun getAuthorizeUrl(): String {
        return "https://accounts.spotify.com/authorize?response_type=code" +
                "&client_id=$clientId" +
                "&scope=user-read-private+user-read-email+playlist-read-private+user-modify-playback-state+user-read-playback-state" +
                "&redirect_uri=http%3A%2F%2Flocalhost%3A8888%2Fcallback"
    }

    override fun authorize(): String = runBlocking {
        httpClient.get("https://accounts.spotify.com/authorize?response_type=code" +
                "&client_id=$clientId" +
                "&scope=user-read-private+user-read-email+playlist-read-private+user-modify-playback-state+user-read-playback-state" +
                "&redirect_uri=http%3A%2F%2Flocalhost%3A8888%2Fcallback").body()
    }

    override fun getTokens(code: String) : String = runBlocking {
            val response : HttpResponse = httpClient.post("https://accounts.spotify.com/api/token") {
                header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}")
                setBody(FormDataContent(Parameters.build {
                    append("code", code)
                    append("redirect_uri", "http://localhost:8888/callback")
                    append("grant_type", "authorization_code")
                }))
                accept(ContentType.Application.Json)
            }
            val content : AuthorizationResponseDto = response.body()
            val userId = getCurrentUserId(content.accessToken, content.refreshToken)!!
            userStorage.save(User(userId, content.accessToken, content.refreshToken))
            userId
        }

    override fun refreshToken(userId: String) = runBlocking {
        val refreshToken = getRefreshToken(userId)
        val response : HttpResponse = httpClient.post("https://accounts.spotify.com/api/token") {
            header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}")
            setBody(FormDataContent(Parameters.build {
                append("refresh_token", refreshToken ?: "")
                append("grant_type", "refresh_token")
            }))
            accept(ContentType.Application.Json)
        }
        val content : RefreshTokenResponseDto = response.body()
        userStorage.save(User(userId, content.accessToken, refreshToken))
    }

    private suspend fun getCurrentUserId(accessToken: String, refreshToken: String) : String? {
        val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me") {
            header("Authorization", "Bearer $accessToken")
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            return null // todo handle this a bit more gracefully
        }
        val currentUser = response.body<CurrentUserResponseDto>()
        return currentUser.id
    }

    override fun getUserPlaylists(userId: String) : CurrentUserPlaylistsResponseDto = runBlocking {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me/playlists") {
                header("Authorization", "Bearer ${getAccessToken(userId)}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 401) {
                refreshToken(userId)
                return@runBlocking getUserPlaylists(userId)
            }
            response.body()
        }

    override fun getPlaylistTracks(userId: String, playlistId: String) : PlaylistTracksResponseDto = runBlocking {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/playlists/$playlistId/tracks") {
                header("Authorization", "Bearer ${getAccessToken(userId)}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 401) {
                refreshToken(userId)
                return@runBlocking getPlaylistTracks(userId, playlistId)
            }
            response.body()
        }

    override suspend fun getTrackAudioAnalysis(userId: String, trackId: String) : TrackAudioAnalysisResponseDto? =
        executeWithCache("getTrackAudioAnalysis_$trackId") {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/audio-analysis/$trackId") {
                header("Authorization", "Bearer ${getAccessToken(userId)}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 404) {
                return null
            }
            if (response.status.value == 401) {
                refreshToken(userId)
                return getTrackAudioAnalysis(userId, trackId)
            }
            response.body()
        }

    override suspend fun getTrackAudioFeatures(userId: String, trackId: String) : TrackAudioFeaturesResponseDto =
        executeWithCache("getTrackAudioFeatures_$trackId") {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/audio-features/$trackId") {
                header("Authorization", "Bearer ${getAccessToken(userId)}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 401) {
                refreshToken(userId)
                return getTrackAudioFeatures(userId, trackId)
            }
            response.body()
        }

    override fun playTrack(userId: String, trackId: String, deviceId: String, positionMs: Int?): String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/play") {
            header("Authorization", "Bearer ${getAccessToken(userId)}")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            parameter("device_id", deviceId)
            setBody(StartPlaybackRequestDto(
                null,
                listOf("spotify:track:$trackId"),
                null,
                positionMs ?: 0
            ))
        }
        if (response.status.value == 401) {
            refreshToken(userId)
            return@runBlocking playTrack(userId, trackId, deviceId, positionMs)
        }
        response.body()
    }

    override fun pausePlayback(userId: String): String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/pause") {
            header("Authorization", "Bearer ${getAccessToken(userId)}")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken(userId)
            return@runBlocking pausePlayback(userId)
        }
        response.body()
    }

    override fun getTrack(userId: String, trackId: String): TrackResponseDto =
        executeWithCache("getTrack_$trackId") {
            runBlocking {
                val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/tracks/$trackId") {
                    header("Authorization", "Bearer ${getAccessToken(userId)}")
                    accept(ContentType.Application.Json)
                }
                if (response.status.value == 401) {
                    refreshToken(userId)
                    return@runBlocking getTrack(userId, trackId)
                }
                response.body()
            }
        }

    override fun seekPlaybackPosition(userId: String, positionMs: Int) : String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/seek") {
            header("Authorization", "Bearer ${getAccessToken(userId)}")
            parameter("position_ms", positionMs)
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken(userId)
            return@runBlocking seekPlaybackPosition(userId, positionMs)
        }
        response.body()
    }

    override fun getPlaybackState(userId: String) : GetPlaybackStateResponseDto? = runBlocking {
        val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me/player") {
            header("Authorization", "Bearer ${getAccessToken(userId)}")
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken(userId)
            return@runBlocking getPlaybackState(userId)
        }
        if (response.status.value == 204) {
            return@runBlocking null
        }
        response.body()
    }

    override fun setVolume(userId: String, volumePercent: Int) : String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/volume") {
            header("Authorization", "Bearer ${getAccessToken(userId)}")
            parameter("volume_percent", volumePercent)
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken(userId)
            return@runBlocking setVolume(userId, volumePercent)
        }
        response.body()
    }

    override fun getAvailableDevices(userId: String): GetAvailableDevicesResponseDto = runBlocking {
        val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me/player/devices") {
            header("Authorization", "Bearer ${getAccessToken(userId)}")
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken(userId)
            return@runBlocking getAvailableDevices(userId)
        }
        response.body()
    }

    private inline fun <reified T> executeWithCache(cacheKey: String, block: () -> T) : T {
        val cachedItem = cacheKey.let {
            cache.get(it, T::class.java)
        }
        if (cachedItem != null) {
            return cachedItem
        }
        val response = block()
        cache.store(cacheKey, response)
        return response
    }

    private fun getAccessToken(userId: String) : String? =
        userStorage.getBySpotifyUserId(userId)?.accessToken

    private fun getRefreshToken(userId: String) : String? =
        userStorage.getBySpotifyUserId(userId)?.refreshToken
}