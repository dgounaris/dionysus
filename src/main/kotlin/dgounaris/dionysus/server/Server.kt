package dgounaris.dionysus.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.common.PropertiesProvider
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import java.text.DateFormat
import java.time.Duration

class Server(
    private val playlistsController: PlaylistsController,
    private val playbackController: PlaybackController,
    private val authorizationController: AuthorizationController,
    private val playbackPlanController: PlaybackPlanController,
    private val statePollingController: StatePollingController
    ) {
    fun start() {
        embeddedServer(Netty, port = 8888) {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    dateFormat = DateFormat.getDateInstance()
                }
            }
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(20)
                timeout = Duration.ofSeconds(15)
            }
            install(Authentication) {
                jwt {
                    verifier(
                        JWT
                        .require(Algorithm.HMAC256(PropertiesProvider.configuration.getProperty("jwtTokenSecret")))
                        .withSubject(PropertiesProvider.configuration.getProperty("jwtTokenSubject"))
                        .withIssuer(PropertiesProvider.configuration.getProperty("jwtTokenIssuer"))
                        .build())

                    validate { credential ->
                        if (credential.payload.getClaim("userId").asString() == "") {
                            null
                        } else {
                            JWTPrincipal(credential.payload)
                        }
                    }
                }
            }
            install(CORS) {
                allowHost(Url(PropertiesProvider.configuration.getProperty("frontendBaseUrl")).host, listOf("http", "https"), listOf("www", ""))
                anyHost()
                allowHeader(HttpHeaders.Authorization)
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Patch)
                allowMethod(HttpMethod.Delete)
                allowNonSimpleContentTypes = true
            }
            playlistsController.configureRouting(this)
            playbackController.configureRouting(this)
            authorizationController.configureRouting(this)
            playbackPlanController.configureRouting(this)
            statePollingController.configureRouting(this)
        }.start(wait = true)
    }
}