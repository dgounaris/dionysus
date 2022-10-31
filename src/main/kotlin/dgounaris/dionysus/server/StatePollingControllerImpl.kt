package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.playback.PlaybackOrchestrator
import dgounaris.dionysus.playback.models.PlaybackState
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.util.collections.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay

class StatePollingControllerImpl(
    private val authorizationController: AuthorizationController,
    private val playbackOrchestrator: PlaybackOrchestrator,
) : StatePollingController {
    override fun configureRouting(application: Application) {
        application.routing {
            authenticate {
                get("v1/state/playback") {
                    val userId = authorizationController.getCurrentUserId(call)
                    call.respond(
                        PlaybackStatusResponseDto(
                            playbackOrchestrator.getCurrentState(userId)
                        )
                    )
                }
            }
            val connections = ConcurrentSet<String>()
            authenticate {
                webSocket("/v1/state") {
                    val userId = authorizationController.getCurrentUserId(call)
                    connections.add(userId)
                    try {
                        for (message in incoming) {
                            outgoing.send(Frame.Text(
                                PlaybackStatusResponseDto(
                                    playbackOrchestrator.getCurrentState(userId)
                                ).toString()
                            ))
                        }
                    } catch (e : Exception) {
                        println(e)
                    } finally {
                        println("Removing user $userId")
                        playbackOrchestrator.onStopEvent(userId)
                        connections.remove(userId)
                    }
                }
            }
        }
    }
}

data class PlaybackStatusResponseDto(
    val playbackState: PlaybackState
)