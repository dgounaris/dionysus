package dgounaris.dionysus.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.common.PropertiesProvider
import dgounaris.dionysus.storage.user.UserStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class AuthorizationControllerImpl(
    private val spotifyClient: SpotifyClient,
    private val userStorage: UserStorage
    ) : AuthorizationController {
    override fun configureRouting(application: Application) {
        application.routing {
            get("/v1/login") {
                call.respond(LoginResponseDto(spotifyClient.getAuthorizeUrl()))
            }
            get("/callback") {
                val userId = callback(call.request.queryParameters["code"]!!, call.request.queryParameters["state"])
                val jwtToken = generateJWT(userId)
                userStorage.getBySpotifyUserId(userId)?.copy(isLoggedIn = true)
                    ?.apply { userStorage.save(this) }
                call.respondRedirect(PropertiesProvider.configuration.getProperty("spotifyCallbackUrl") + "?token=" + jwtToken, false)
            }
            authenticate(optional = true) {
                get("/v1/login/status") {
                    if (call.principal<JWTPrincipal>() == null ||
                        userStorage.getBySpotifyUserId(getCurrentUserId(call)) == null ||
                        userStorage.getBySpotifyUserId(getCurrentUserId(call))?.isLoggedIn == false
                    ) {
                        call.respond(HttpStatusCode.NotFound)
                    }
                    else {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
            authenticate {
                post("/v1/logout") {
                    val userId = getCurrentUserId(call)
                    userStorage.getBySpotifyUserId(userId)?.copy(isLoggedIn = false, accessToken = null, refreshToken = null)
                        ?.apply { userStorage.save(this) }
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    fun generateJWT(userId: String) = JWT.create()
        .withIssuer(PropertiesProvider.configuration.getProperty("jwtTokenIssuer"))
        .withSubject(PropertiesProvider.configuration.getProperty("jwtTokenSubject"))
        .withClaim("userId", userId)
        .sign(Algorithm.HMAC256(PropertiesProvider.configuration.getProperty("jwtTokenSecret")))

    override fun getCurrentUserId(call: ApplicationCall): String {
        val principal = call.principal<JWTPrincipal>()
        return principal!!.payload.getClaim("userId").asString()
    }

    private fun callback(code: String, state: String?) : String =
        spotifyClient.getTokens(code)
}

data class LoginResponseDto(
    val loginUrl: String
)