package dgounaris.dionysus.server

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.client.features.json.*
import io.ktor.client.request.forms.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.text.DateFormat

class Server(
    private val authorizationController: AuthorizationController,
    private val playlistsController: PlaylistsController,
    private val tracksController: TracksController,
    private val playbackController: PlaybackController
    ) {
    fun start() {
        embeddedServer(Netty, port = 8888, host = "localhost") {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    dateFormat = DateFormat.getDateInstance()
                }
            }
            authorizationController.configureRouting(this)
            playlistsController.configureRouting(this)
            tracksController.configureRouting(this)
            playbackController.configureRouting(this)
        }.start(wait = true)
    }
}