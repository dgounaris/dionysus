package dgounaris.dionysus.server

import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.dionysense.FeedbackHandler
import dgounaris.dionysus.dionysense.TrackSectionSelector
import dgounaris.dionysus.playback.PlaybackHandler
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlin.concurrent.thread

class PlaybackControllerImpl(
    private val trackSectionSelector: TrackSectionSelector,
    private val playbackHandler: PlaybackHandler,
    private val feedbackHandler: FeedbackHandler
    ): PlaybackController {
    override fun configureRouting(application: Application) {
        application.routing {
            post("/playback/play") {
                val formParameters = call.receiveParameters()
                thread { play(formParameters) }
                call.respondText("Playback started successfully")
            }
            post("/playback/play/auto") {
                val formParameters = call.receiveParameters()
                thread { autoplay(formParameters) }
                call.respondHtml { responseAutoplayStartedOk(this) }
            }
            post("/playback/feedback") {
                submitFeedback()
            }
        }
    }

    private fun autoplay(params: Parameters) {
        val targetDeviceId = params.entries().single { it.key == "device_select" }.value.single()
        val targetTracks = params.entries()
            .filter { entry -> entry.key.startsWith("trackSection_") }
            .map { it.key.substringAfter("trackSection_") }
        val targetSections = runBlocking {
            targetTracks
                .parallelMap { trackId ->
                    val sections = trackSectionSelector.selectSections(trackId)
                    TrackSections(trackId, sections.map { section -> TrackSectionStartEnd(section.start, section.end) })
                }
        }
        val playlistId = params.entries().first { entry -> entry.key.startsWith("playlistId") }.value.first()
        playbackHandler.play(playlistId, targetSections, targetDeviceId)
    }

    private fun play(params: Parameters) {
        val targetDeviceId = params.entries().single { it.key == "device_select" }.value.single()
        val targetSections = params.entries()
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
        playbackHandler.play(playlistId, targetSections, targetDeviceId)
    }

    private fun submitFeedback() {
        feedbackHandler.handleFeedback()
    }

    private fun responseAutoplayStartedOk(html: HTML) {
        html.body {
            p {
                +"Playback started successfully"
            }
            br
            p {
                +"For feedback on inaccurate selection please press: "
                form {
                    action = "http://localhost:8888/playback/feedback"
                    method = FormMethod.post
                    input {
                        type = InputType.submit
                        value = "Submit feedback"
                    }
                }
            }
        }
    }
}