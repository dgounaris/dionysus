package dgounaris.dionysus.auth

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.storage.user.UserStorage
import dgounaris.dionysus.user.models.User
import dgounaris.dionysus.view.loginView
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*

class AuthorizationControllerImpl(
    private val spotifyClient: SpotifyClient,
    private val userStorage: UserStorage
    ) : AuthorizationController {
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

    override fun getCurrentUser(): User? =
        userStorage.getBySpotifyUserId("")

    private fun login(html: HTML) {
        val authUrl = spotifyClient.getAuthorizeUrl()
        loginView(html, authUrl)
    }

    private fun callback(code: String, state: String?) {
        spotifyClient.getTokens(code)
    }
}