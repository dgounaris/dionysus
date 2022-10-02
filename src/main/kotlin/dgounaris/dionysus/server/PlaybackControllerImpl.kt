package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.dionysense.FeedbackHandler
import dgounaris.dionysus.dionysense.TrackOrderSelector
import dgounaris.dionysus.dionysense.TrackSectionSelector
import dgounaris.dionysus.playback.PlaybackOrchestrator
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import dgounaris.dionysus.view.postAutoplayView
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
    private val playlistDetailsProvider: PlaylistDetailsProvider,
    private val trackSectionSelector: TrackSectionSelector,
    private val trackOrderSelector: TrackOrderSelector,
    private val playbackOrchestrator: PlaybackOrchestrator,
    private val feedbackHandler: FeedbackHandler,
    private val authorizationController: AuthorizationController
    ): PlaybackController {
    override fun configureRouting(application: Application) {
        application.routing {
            post("/playback/play/auto") {
                if (!authorizationController.isAuthorized("")) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
                val formParameters = call.receiveParameters()
                call.respondHtml { autoplay(formParameters, this) }
            }
            post("/playback/feedback") {
                submitFeedback()
            }
            post("/playback/stop") {
                val formParameters = call.receiveParameters()
                call.respondHtml { stopPlayback(formParameters, this) }
            }
            post("/playback/pause") {
                val formParameters = call.receiveParameters()
                call.respondHtml { pausePlayback(formParameters, this) }
            }
            post("/playback/resume") {
                val formParameters = call.receiveParameters()
                call.respondHtml { resumePlayback(formParameters, this) }
            }
            post("/playback/next") {
                val formParameters = call.receiveParameters()
                call.respondHtml { nextPlayback(formParameters, this) }
            }
        }
    }

    private fun autoplay(params: Parameters, html: HTML) {
        val targetDeviceDetails = params.entries().single { it.key == "device_select" }.value.single()
        val playbackDetails = PlaybackDetails(
            targetDeviceDetails.split("-")[0],
            targetDeviceDetails.split("-")[1],
            targetDeviceDetails.split("-")[2].toInt()
        )
        val playlistName = params.entries().single { it.key.startsWith("playlistId_") }.value.single()
        val targetPlaylist = playlistDetailsProvider.getPlaylistDetails(playlistName)
        val targetTracksWithCustomOrder = runBlocking { trackOrderSelector.selectOrder(targetPlaylist.tracks.map { it.id }) }
        val targetSections = runBlocking {
            targetTracksWithCustomOrder
                .parallelMap { trackId ->
                    Pair(trackId, trackSectionSelector.selectSections(trackId))
                }
        }
        val trackSections = targetSections.map {
            TrackSections(it.first, it.second.map { section -> TrackSectionStartEnd(section.start, section.end) })
        }
        thread { playbackOrchestrator.play("", trackSections, playbackDetails) }
        responseAutoplayStartedOk(html)
    }

    private fun stopPlayback(params: Parameters, html: HTML) {
        thread { playbackOrchestrator.onStopEvent("") }
    }

    private fun pausePlayback(params: Parameters, html: HTML) {
        thread { playbackOrchestrator.onPauseEvent("") }
    }

    private fun resumePlayback(params: Parameters, html: HTML) {
        thread { playbackOrchestrator.onResumeEvent("") }
    }

    private fun nextPlayback(params: Parameters, html: HTML) {
        thread { playbackOrchestrator.onNextEvent("") }
    }

    private fun submitFeedback() {
        feedbackHandler.handleFeedback()
    }

    private fun responseAutoplayStartedOk(html: HTML) {
        postAutoplayView(html)
    }
}