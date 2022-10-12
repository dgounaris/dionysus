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
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class PlaybackControllerImpl(
    private val playlistDetailsProvider: PlaylistDetailsProvider,
    private val trackSectionSelector: TrackSectionSelector,
    private val trackOrderSelector: TrackOrderSelector,
    private val playbackOrchestrator: PlaybackOrchestrator,
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
            authenticate {
                post("/v1/playback/play/auto") {
                    val userId = authorizationController.getCurrentUserId(call)
                    val requestBody = call.receive<AutoplayRequestDto>()
                    autoplayV1(userId, requestBody)
                    call.respond(HttpStatusCode.OK)
                }
            }
            authenticate {
                post("/v1/playback/stop") {
                    val userId = authorizationController.getCurrentUserId(call)
                    stopPlaybackV1(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }
            authenticate {
                post("/v1/playback/pause") {
                    val userId = authorizationController.getCurrentUserId(call)
                    pausePlaybackV1(userId)
                    call.respond(HttpStatusCode.OK)
                }
            }
            authenticate {
                post("/v1/playback/resume") {
                    val userId = authorizationController.getCurrentUserId(call)
                    resumePlaybackV1(userId)
                    call.respond(HttpStatusCode.OK)
                }
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

    private fun stopPlaybackV1(userId: String) {
        playbackOrchestrator.onStopEvent(userId)
    }

    private fun pausePlaybackV1(userId: String) {
        playbackOrchestrator.onPauseEvent(userId)
    }

    private fun resumePlaybackV1(userId: String) {
        playbackOrchestrator.onResumeEvent(userId)
    }

    private fun nextPlaybackV1(userId: String) {
        playbackOrchestrator.onNextEvent(userId)
    }
}

data class AutoplayRequestDto(
    val playbackDetails: PlaybackDetails,
    val playlistName: String
)