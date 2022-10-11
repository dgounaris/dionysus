package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.dionysense.FeedbackHandler
import dgounaris.dionysus.dionysense.TrackOrderSelector
import dgounaris.dionysus.dionysense.TrackSectionSelector
import dgounaris.dionysus.playback.PlaybackOrchestrator
import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import dgounaris.dionysus.view.postAutoplayView
import io.ktor.application.*
import io.ktor.auth.*
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
            authenticate {
                get("/v1/playback/devices") {
                    val userId = authorizationController.getCurrentUserId(call)
                    call.respond(getAvailablePlaybackDevicesV1(userId))
                }
            }
            post("/playback/play/auto") {
                if (!authorizationController.isAuthorized("")) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
                val formParameters = call.receiveParameters()
                call.respondHtml { autoplay(formParameters, this) }
            }
            authenticate {
                post("/v1/playback/play/auto") {
                    val userId = authorizationController.getCurrentUserId(call)
                    val requestBody = call.receive<AutoplayRequestDto>()
                    autoplayV1(userId, requestBody)
                    call.respond(HttpStatusCode.OK)
                }
            }
            post("/playback/feedback") {
                submitFeedback()
            }
            post("/playback/stop") {
                if (!authorizationController.isAuthorized("")) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
                val formParameters = call.receiveParameters()
                call.respondHtml { stopPlayback(formParameters, this) }
            }
            authenticate {
                post("/v1/playback/stop") {
                    val userId = authorizationController.getCurrentUserId(call)
                    stopPlaybackV1(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }
            post("/playback/pause") {
                if (!authorizationController.isAuthorized("")) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
                val formParameters = call.receiveParameters()
                call.respondHtml { pausePlayback(formParameters, this) }
            }
            authenticate {
                post("/v1/playback/pause") {
                    val userId = authorizationController.getCurrentUserId(call)
                    pausePlaybackV1(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }
            post("/playback/resume") {
                if (!authorizationController.isAuthorized("")) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
                val formParameters = call.receiveParameters()
                call.respondHtml { resumePlayback(formParameters, this) }
            }
            authenticate {
                post("/v1/playback/resume") {
                    val userId = authorizationController.getCurrentUserId(call)
                    resumePlaybackV1(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }
            post("/playback/next") {
                if (!authorizationController.isAuthorized("")) {
                    return@post call.respond(HttpStatusCode.Unauthorized)
                }
                val formParameters = call.receiveParameters()
                call.respondHtml { nextPlayback(formParameters, this) }
            }
            authenticate {
                post("/v1/playback/next") {
                    val userId = authorizationController.getCurrentUserId(call)
                    nextPlaybackV1(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun getAvailablePlaybackDevicesV1(userId: String) : List<AvailableDevice> {
        return playbackOrchestrator.getAvailableDevices(userId)
    }

    private fun autoplay(params: Parameters, html: HTML) {
        val userId = authorizationController.getCurrentUserId()
        val targetDeviceDetails = params.entries().single { it.key == "device_select" }.value.single()
        val playbackDetails = PlaybackDetails(
            targetDeviceDetails.split("-")[0],
            targetDeviceDetails.split("-")[1],
            targetDeviceDetails.split("-")[2].toInt()
        )
        val playlistName = params.entries().single { it.key.startsWith("playlistId_") }.value.single()
        val targetPlaylist = playlistDetailsProvider.getPlaylistDetails(authorizationController.getCurrentUserId(), playlistName)
        val targetTracksWithCustomOrder = runBlocking { trackOrderSelector.selectOrder(userId, targetPlaylist.tracks.map { it.id }) }
        val targetSections = runBlocking {
            targetTracksWithCustomOrder
                .parallelMap { trackId ->
                    Pair(trackId, trackSectionSelector.selectSections(userId, trackId))
                }
        }
        val trackSections = targetSections.map {
            TrackSections(it.first, it.second.map { section -> TrackSectionStartEnd(section.start, section.end) })
        }
        thread { playbackOrchestrator.play(authorizationController.getCurrentUserId(), trackSections, playbackDetails) }
        responseAutoplayStartedOk(html)
    }

    private fun autoplayV1(userId: String, body: AutoplayRequestDto) {
        val playbackDetails = body.playbackDetails
        val playlistName = body.playlistName

        val targetPlaylist = playlistDetailsProvider.getPlaylistDetails(userId, playlistName)
        val targetTracksWithCustomOrder = runBlocking { trackOrderSelector.selectOrder(userId, targetPlaylist.tracks.map { it.id }) }
        val targetSections = runBlocking {
            targetTracksWithCustomOrder
                .parallelMap { trackId ->
                    Pair(trackId, trackSectionSelector.selectSections(userId, trackId))
                }
        }
        val trackSections = targetSections.map {
            TrackSections(it.first, it.second.map { section -> TrackSectionStartEnd(section.start, section.end) })
        }
        thread { playbackOrchestrator.play(userId, trackSections, playbackDetails) }
    }

    private fun stopPlayback(params: Parameters, html: HTML) {
        playbackOrchestrator.onStopEvent(authorizationController.getCurrentUserId())
        postAutoplayView(html)
    }

    private fun stopPlaybackV1(userId: String) {
        playbackOrchestrator.onStopEvent(userId)
    }

    private fun pausePlayback(params: Parameters, html: HTML) {
        playbackOrchestrator.onPauseEvent(authorizationController.getCurrentUserId())
        postAutoplayView(html)
    }

    private fun pausePlaybackV1(userId: String) {
        playbackOrchestrator.onPauseEvent(userId)
    }

    private fun resumePlayback(params: Parameters, html: HTML) {
        playbackOrchestrator.onResumeEvent(authorizationController.getCurrentUserId())
        postAutoplayView(html)
    }

    private fun resumePlaybackV1(userId: String) {
        playbackOrchestrator.onResumeEvent(userId)
    }

    private fun nextPlayback(params: Parameters, html: HTML) {
        playbackOrchestrator.onNextEvent(authorizationController.getCurrentUserId())
        postAutoplayView(html)
    }

    private fun nextPlaybackV1(userId: String) {
        playbackOrchestrator.onNextEvent(userId)
    }

    private fun submitFeedback() {
        val userId = authorizationController.getCurrentUserId()
        feedbackHandler.handleFeedback(userId)
    }

    private fun responseAutoplayStartedOk(html: HTML) {
        postAutoplayView(html)
    }
}

data class AutoplayRequestDto(
    val playbackDetails: PlaybackDetails,
    val playlistName: String
)