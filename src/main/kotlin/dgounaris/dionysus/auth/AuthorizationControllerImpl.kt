package dgounaris.dionysus.auth

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.common.PropertiesProvider
import dgounaris.dionysus.storage.user.UserStorage
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
            get("/v1/login") {
                call.respondRedirect(spotifyClient.getAuthorizeUrl())
            }
            get("/callback") {
                callback(call.request.queryParameters["code"]!!, call.request.queryParameters["state"])

                call.respondRedirect(PropertiesProvider.configuration.getProperty("spotifyCallbackUrl"), false)
            }
        }
    }

    override fun isAuthorized(user: String): Boolean {
        return userStorage.getBySpotifyUserId("") != null
    }

    override fun getCurrentUserId(): String {
        return ""
    }

    private fun login(html: HTML) {
        val authUrl = spotifyClient.getAuthorizeUrl()
        loginView(html, authUrl)
    }

    private fun callback(code: String, state: String?) {
        spotifyClient.getTokens(code)
    }
}