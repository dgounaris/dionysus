package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.playback.PlaybackOrchestrator
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackDetails
import dgounaris.dionysus.view.currentUserPlaylistsSelectionView
import dgounaris.dionysus.view.playlistTracksView
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*

class PlaylistsControllerImpl(
    private var playlistDetailsProvider: PlaylistDetailsProvider,
    private val playbackOrchestrator: PlaybackOrchestrator,
    private val trackDetailsProvider: TrackDetailsProvider,
    private val authorizationController: AuthorizationController
    ) : PlaylistsController {
    override fun configureRouting(application: Application) {
        application.routing {
            get("/playlists/tracks") {
                if (!authorizationController.isAuthorized("")) {
                    return@get call.respond(HttpStatusCode.Unauthorized)
                }
                val playlistId = call.request.queryParameters["playlistName"]
                    ?: call.receiveParameters()["playlistName"]!!
                call.respondHtml { getPlaylistTracks(playlistId, this) }
            }
            get("/playlists/me") {
                if (!authorizationController.isAuthorized("")) {
                    return@get call.respond(HttpStatusCode.Unauthorized)
                }
                call.respondHtml { getCurrentUserPlaylists(this) }
            }
        }
    }

    private fun getPlaylistTracks(playlistName: String, html: HTML) {
        val userId = authorizationController.getCurrentUserId()
        val playlist = playlistDetailsProvider.getPlaylistDetails(userId, playlistName)
        val playlistTrackDetails = runBlocking {
            playlist.tracks.parallelMap { track ->
                val trackDetails = trackDetailsProvider.getTrackDetails(userId, track.id)
                trackDetails.toTrackDetailsResponseDto()
            }
        }
        val playlistResponse = PlaylistResponseDto(
            playlist.name, playlist.id, playlistTrackDetails
        )
        val availablePlaybackDevices = playbackOrchestrator.getAvailableDevices(userId)
        playlistTracksView(html, playlistResponse, availablePlaybackDevices)
    }

    private fun getCurrentUserPlaylists(html: HTML) {
        val userId = authorizationController.getCurrentUserId()
        val currentUserPlaylists = playlistDetailsProvider.getUserPlaylistNames(userId)
        currentUserPlaylistsSelectionView(html, currentUserPlaylists)
    }
}

data class PlaylistResponseDto(
    val name: String,
    val id: String,
    val trackDetails: List<TrackDetailsResponseDto>
)

data class TrackDetailsResponseDto(
    val id: String,
    val name: String,
    val sections: List<TrackSectionResponseDto>
)

data class TrackSectionResponseDto(
    val start : Double,
    val end : Double,
    val tempo : Double,
    val key : Int,
    val mode : Int,
    val timeSignature : Int
)

fun TrackDetails.toTrackDetailsResponseDto() =
    TrackDetailsResponseDto(this.id, this.name, this.sections.map { section ->
        TrackSectionResponseDto(
            section.start,
            section.end,
            section.tempo,
            section.key,
            section.mode,
            section.timeSignature
        )
    })