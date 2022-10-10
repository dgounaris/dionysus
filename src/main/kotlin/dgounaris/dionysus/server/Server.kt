package dgounaris.dionysus.server

import com.fasterxml.jackson.databind.SerializationFeature
import dgounaris.dionysus.auth.AuthorizationController
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.text.DateFormat

class Server(
    private val playlistsController: PlaylistsController,
    private val tracksController: TracksController,
    private val playbackController: PlaybackController,
    private val authorizationController: AuthorizationController
    ) {
    fun start() {
        embeddedServer(Netty, port = 8888) {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    dateFormat = DateFormat.getDateInstance()
                }
            }
            install(CORS) {
                //host("localhost:3000", listOf("http", "https"), listOf("www", ""))
                anyHost()
                method(HttpMethod.Get)
                method(HttpMethod.Post)
                method(HttpMethod.Options)
                method(HttpMethod.Put)
                method(HttpMethod.Patch)
                method(HttpMethod.Delete)
                allowNonSimpleContentTypes = true
            }
            playlistsController.configureRouting(this)
            tracksController.configureRouting(this)
            playbackController.configureRouting(this)
            authorizationController.configureRouting(this)
        }.start(wait = true)
    }
}