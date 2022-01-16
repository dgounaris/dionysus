package dgounaris.dionysus.server

import dgounaris.dionysus.clients.SpotifyClient
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

class AuthorizationControllerImpl(private val spotifyClient: SpotifyClient) : AuthorizationController {
    override fun configureRouting(application: Application) {
        application.routing {
            get("/test") {
                call.respondText { "Hello World!" }
            }
            get("/login") {
                call.respondHtml {
                    login(this)
                }
            }
            get("/callback") {
                callback(call.request.queryParameters["code"]!!, call.request.queryParameters["state"])
                call.respondRedirect("http://localhost:8888/playlists/me", false)
            }
        }
    }

    private fun login(html: HTML) {
        val authUrl = spotifyClient.getAuthorizeUrl()
        html.body {
            p {
                +"Click this link to login: "
                a(authUrl) { +"Login" }
            }
        }
    }

    private fun callback(code: String, state: String?) {
        spotifyClient.getTokens(code)
    }
}