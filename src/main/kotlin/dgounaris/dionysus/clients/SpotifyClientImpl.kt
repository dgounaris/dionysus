package dgounaris.dionysus.clients

import com.fasterxml.jackson.databind.DeserializationFeature
import dgounaris.dionysus.clients.cache.Cache
import dgounaris.dionysus.clients.models.*
import dgounaris.dionysus.common.PropertiesProvider
import dgounaris.dionysus.storage.user.UserStorage
import dgounaris.dionysus.user.models.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.json.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
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

    override fun getTokens(code: String) = runBlocking {
            val response : HttpResponse = httpClient.post("https://accounts.spotify.com/api/token") {
                header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}")
                body = FormDataContent(Parameters.build {
                    append("code", code)
                    append("redirect_uri", "http://localhost:8888/callback")
                    append("grant_type", "authorization_code")
                })
                accept(ContentType.Application.Json)
            }
            val content : AuthorizationResponseDto = response.receive()
            println("Access token: ${content.accessToken}")
            println("Refresh token: ${content.refreshToken}")
            userStorage.save(User("", content.accessToken, content.refreshToken))
        }

    override fun refreshToken() = runBlocking {
        val refreshToken = getRefreshToken()
        val response : HttpResponse = httpClient.post("https://accounts.spotify.com/api/token") {
            header("Authorization", "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}")
            body = FormDataContent(Parameters.build {
                append("refresh_token", refreshToken ?: "")
                append("grant_type", "refresh_token")
            })
            accept(ContentType.Application.Json)
        }
        val content : RefreshTokenResponseDto = response.receive()
        println("Access token: ${content.accessToken}")
        userStorage.save(User("", content.accessToken, refreshToken))
    }

    override fun getCurrentUser() : String = runBlocking {
        TODO("Not yet implemented")
        val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me") {
            header("Authorization", "Bearer ${getAccessToken()}")
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking getCurrentUser()
        }
        response.receive()
    }

    override fun getCurrentUserPlaylists() : CurrentUserPlaylistsResponseDto = runBlocking {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me/playlists") {
                header("Authorization", "Bearer ${getAccessToken()}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 401) {
                refreshToken()
                return@runBlocking getCurrentUserPlaylists()
            }
            response.receive()
        }

    override fun getPlaylistTracks(playlistId: String) : PlaylistTracksResponseDto = runBlocking {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/playlists/$playlistId/tracks") {
                header("Authorization", "Bearer ${getAccessToken()}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 401) {
                refreshToken()
                return@runBlocking getPlaylistTracks(playlistId)
            }
            response.receive()
        }

    override suspend fun getTrackAudioAnalysis(trackId: String) : TrackAudioAnalysisResponseDto? =
        executeWithCache("getTrackAudioAnalysis_$trackId") {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/audio-analysis/$trackId") {
                header("Authorization", "Bearer ${getAccessToken()}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 404) {
                return null
            }
            if (response.status.value == 401) {
                refreshToken()
                return getTrackAudioAnalysis(trackId)
            }
            response.receive()
        }

    override suspend fun getTrackAudioFeatures(trackId: String) : TrackAudioFeaturesResponseDto =
        executeWithCache("getTrackAudioFeatures_$trackId") {
            val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/audio-features/$trackId") {
                header("Authorization", "Bearer ${getAccessToken()}")
                accept(ContentType.Application.Json)
            }
            if (response.status.value == 401) {
                refreshToken()
                return getTrackAudioFeatures(trackId)
            }
            response.receive()
        }

    override fun playTrack(trackId: String, deviceId: String, positionMs: Int?): String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/play") {
            header("Authorization", "Bearer ${getAccessToken()}")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            parameter("device_id", deviceId)
            body = StartPlaybackRequestDto(
                null,
                listOf("spotify:track:$trackId"),
                null,
                positionMs ?: 0
            )
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking playTrack(trackId, deviceId, positionMs)
        }
        response.receive()
    }

    override fun startPlayback(deviceId: String): String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/play") {
            header("Authorization", "Bearer ${getAccessToken()}")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            parameter("device_id", deviceId)
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking startPlayback(deviceId)
        }
        response.receive()
    }

    override fun addToPlaybackQueue(trackId: String): String = runBlocking {
        val response : HttpResponse = httpClient.post("https://api.spotify.com/v1/me/player/queue") {
            header("Authorization", "Bearer ${getAccessToken()}")
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            parameter("uri", "spotify:track:$trackId")
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking addToPlaybackQueue(trackId)
        }
        response.receive()
    }

    override fun getTrack(trackId: String): TrackResponseDto =
        executeWithCache("getTrack_$trackId") {
            runBlocking {
                val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/tracks/$trackId") {
                    header("Authorization", "Bearer ${getAccessToken()}")
                    accept(ContentType.Application.Json)
                }
                if (response.status.value == 401) {
                    refreshToken()
                    return@runBlocking getTrack(trackId)
                }
                response.receive()
            }
        }

    override fun playNext() : String = runBlocking {
        val response : HttpResponse = httpClient.post("https://api.spotify.com/v1/me/player/next") {
            header("Authorization", "Bearer ${getAccessToken()}")
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking playNext()
        }
        response.receive()
    }

    override fun seekPlaybackPosition(positionMs: Int) : String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/seek") {
            header("Authorization", "Bearer ${getAccessToken()}")
            parameter("position_ms", positionMs)
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking seekPlaybackPosition(positionMs)
        }
        response.receive()
    }

    override fun getPlaybackState() : GetPlaybackStateResponseDto? = runBlocking {
        val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me/player") {
            header("Authorization", "Bearer ${getAccessToken()}")
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking getPlaybackState()
        }
        if (response.status.value == 204) {
            return@runBlocking null
        }
        response.receive()
    }

    override fun setVolume(volumePercent: Int) : String = runBlocking {
        val response : HttpResponse = httpClient.put("https://api.spotify.com/v1/me/player/volume") {
            header("Authorization", "Bearer ${getAccessToken()}")
            parameter("volume_percent", volumePercent)
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking setVolume(volumePercent)
        }
        response.receive()
    }

    override fun getAvailableDevices(): GetAvailableDevicesResponseDto = runBlocking {
        val response : HttpResponse = httpClient.get("https://api.spotify.com/v1/me/player/devices") {
            header("Authorization", "Bearer ${getAccessToken()}")
            accept(ContentType.Application.Json)
        }
        if (response.status.value == 401) {
            refreshToken()
            return@runBlocking getAvailableDevices()
        }
        response.receive()
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

    private fun getAccessToken() : String? =
        userStorage.getBySpotifyUserId("")?.accessToken

    private fun getRefreshToken() : String? =
        userStorage.getBySpotifyUserId("")?.refreshToken
}