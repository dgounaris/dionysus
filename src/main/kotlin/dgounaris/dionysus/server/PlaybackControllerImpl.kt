package dgounaris.dionysus.server

import dgounaris.dionysus.playback.PlaybackHandler
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.concurrent.thread

class PlaybackControllerImpl(
    private val playlistDetailsProvider: PlaylistDetailsProvider,
    private val playbackHandler: PlaybackHandler
    ): PlaybackController {
    override fun configureRouting(application: Application) {
        application.routing {
            post("/playback/play") {
                val formParameters = call.receiveParameters()
                thread { play(formParameters) }
                call.respondText("Playback started successfully")
            }
        }
    }

    private fun play(params: Parameters) {
        val targetSegments = params.entries()
            .filter { entry -> entry.key.startsWith("trackSection_") }
            .map { entry ->
                val values = entry.value
                val key = entry.key
                TrackSections(
                    key.substringAfter("trackSection_"),
                    values.filter { value -> value.isNotBlank() }
                            .map { value -> value.split("-") }
                            .map { list ->
                                TrackSectionStartEnd(list.elementAt(0).toDouble(), list.elementAt(1).toDouble())
                            }
                )
            }
        val playlistId = params.entries().first { entry -> entry.key.startsWith("playlistId") }.value.first()
        playbackHandler.play(playlistId, targetSegments)
    }
}