package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackDetails
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking

class PlaylistsControllerImpl(
    private var playlistDetailsProvider: PlaylistDetailsProvider,
    private val trackDetailsProvider: TrackDetailsProvider,
    private val authorizationController: AuthorizationController
    ) : PlaylistsController {
    override fun configureRouting(application: Application) {
        application.routing {
            authenticate {
                get("/v1/playlists/me") {
                    val userId = authorizationController.getCurrentUserId(call)
                    call.respond(getCurrentUserPlaylistsV1(userId))
                }
            }
            authenticate {
                get("/v1/playlists/tracks") {
                    val userId = authorizationController.getCurrentUserId(call)
                    val playlistId = call.request.queryParameters["playlistName"]
                        ?: call.receiveParameters()["playlistName"]!!
                    call.respond(getPlaylistTracksV1(userId, playlistId))
                }
            }
        }
    }

    private fun getCurrentUserPlaylistsV1(userId: String) : List<String> {
        return playlistDetailsProvider.getUserPlaylistNames(userId)
    }

    private fun getPlaylistTracksV1(userId: String, playlistName: String) : PlaylistResponseDto {
        val playlist = playlistDetailsProvider.getPlaylistDetails(userId, playlistName)
        val playlistTrackDetails = runBlocking {
            playlist.tracks.parallelMap { track -> trackDetailsProvider.getTrackDetails(userId, track.id) }
        }
        return PlaylistResponseDto(
            playlist.name, playlist.id, playlistTrackDetails
        )
    }
}

data class PlaylistResponseDto(
    val name: String,
    val id: String,
    val trackDetails: List<TrackDetails>
)