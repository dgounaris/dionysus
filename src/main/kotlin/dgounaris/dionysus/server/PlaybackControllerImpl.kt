package dgounaris.dionysus.server

import dgounaris.dionysus.playback.PlaybackHandler
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class PlaybackControllerImpl(
    private val playlistDetailsProvider: PlaylistDetailsProvider,
    private val playbackHandler: PlaybackHandler
    ): PlaybackController {
    override fun configureRouting(application: Application) {
        application.routing {
            post("/playback/play") {
                val formParameters = call.receiveParameters()
                //val body = call.receive<PlaybackPlayRequestDto>()
                thread { play(formParameters) }
                call.respondText("Playback started successfully")
            }
        }
    }

    private fun play(params: Parameters) {
        val targetTrackMs = params.entries()
            .filter { entry -> entry.key.startsWith("trackSection_") }
            .map { entry ->
                val values = entry.value
                values.filter { value -> value.isNotBlank() }
                        .map { value -> value.split("-") }
                        .map { list ->
                            (1000 * (list.elementAt(0).toDouble() + list.elementAt(1).toDouble())/2).roundToInt()
                        }
            }
        val playlistName = params.entries().first { entry -> entry.key.startsWith("playlistName") }.value.first()
        val details = playlistDetailsProvider.getPlaylistDetails(playlistName)
        playbackHandler.play(details, targetTrackMs)
    }

    private fun play(body: PlaybackPlayRequestDto) {
        val details = playlistDetailsProvider.getPlaylistDetails(body.playlistName)
        playbackHandler.play(details, body.targetTrackMs)
    }
}

data class PlaybackPlayRequestDto(
    val playlistName: String,
    val targetTrackMs: List<List<Int>>
)