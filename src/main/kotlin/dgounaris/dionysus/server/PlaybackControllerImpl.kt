package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.playback.PlaybackOrchestrator
import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playback.models.PlaybackState
import dgounaris.dionysus.storage.user.UserStorage
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.concurrent.thread

class PlaybackControllerImpl(
    private val playbackOrchestrator: PlaybackOrchestrator,
    private val authorizationController: AuthorizationController,
    private val userStorage: UserStorage
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
                    call.respond(HttpStatusCode.OK, PlaybackUpdateResponseDto(PlaybackState.PLAYING))
                }
            }
            authenticate {
                post("/v1/playback/stop") {
                    val userId = authorizationController.getCurrentUserId(call)
                    stopPlaybackV1(userId)
                    call.respond(HttpStatusCode.OK, PlaybackUpdateResponseDto(PlaybackState.STOPPED))
                }
            }
            authenticate {
                post("/v1/playback/pause") {
                    val userId = authorizationController.getCurrentUserId(call)
                    pausePlaybackV1(userId)
                    call.respond(HttpStatusCode.OK, PlaybackUpdateResponseDto(PlaybackState.PAUSED))
                }
            }
            authenticate {
                post("/v1/playback/resume") {
                    val userId = authorizationController.getCurrentUserId(call)
                    resumePlaybackV1(userId)
                    call.respond(HttpStatusCode.OK, PlaybackUpdateResponseDto(PlaybackState.PLAYING))
                }
            }
            authenticate {
                post("/v1/playback/next") {
                    val userId = authorizationController.getCurrentUserId(call)
                    nextPlaybackV1(userId)
                    call.respond(HttpStatusCode.OK, PlaybackUpdateResponseDto(PlaybackState.PLAYING))
                }
            }
            authenticate {
                get("/v1/playback/client") {
                    val userId = authorizationController.getCurrentUserId(call)
                    call.respond(
                        PlaybackClientResponseDto(
                            userStorage.getBySpotifyUserId(userId)?.accessToken
                        )
                    )
                }
            }
        }
    }

    private fun getAvailablePlaybackDevicesV1(userId: String) : List<AvailableDevice> {
        return playbackOrchestrator.getAvailableDevices(userId)
    }

    private fun autoplayV1(userId: String, body: AutoplayRequestDto) {
        val playbackDetails = body.playbackDetails

        thread { playbackOrchestrator.play(userId, playbackDetails) }
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
    val playbackDetails: PlaybackDetails
)

data class PlaybackUpdateResponseDto(
    val playbackState: PlaybackState
)

data class PlaybackClientResponseDto(
    val token: String?
)