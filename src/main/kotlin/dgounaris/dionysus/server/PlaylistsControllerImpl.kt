package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackDetails
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
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
            playlist.tracks.parallelMap { track ->
                val trackDetails = trackDetailsProvider.getTrackDetails(userId, track.id)
                trackDetails.toTrackDetailsResponseDto()
            }
        }
        val playlistResponse = PlaylistResponseDto(
            playlist.name, playlist.id, playlistTrackDetails
        )
        return playlistResponse
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