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
import java.time.Clock
import java.time.Instant

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
                val refreshToken = generateRefreshToken(userId)
                userStorage.getBySpotifyUserId(userId)?.copy(isLoggedIn = true)
                    ?.apply { userStorage.save(this) }
                call.respondRedirect(PropertiesProvider.configuration.getProperty("frontendBaseUrl")
                        + "/login/callback"
                        + "?token=" + jwtToken
                        + "&refresh=" + refreshToken, false)
            }
            authenticate(optional = true) {
                get("/v1/login/status") {
                    if (call.principal<JWTPrincipal>() == null ||
                        userStorage.getBySpotifyUserId(getCurrentUserId(call)) == null ||
                        userStorage.getBySpotifyUserId(getCurrentUserId(call))?.isLoggedIn == false
                    ) {
                        call.respond(LoginStatusResponseDto(false, ""))
                    }
                    else {
                        call.respond(LoginStatusResponseDto(true, getCurrentUserId(call)))
                    }
                }
            }
            authenticate {
                get("/v1/login/refresh") {
                    val userId = getCurrentUserId(call)
                    call.respond(RefreshTokenResponseDto(generateJWT(userId)))
                }
            }
            authenticate {
                post("/v1/logout") {
                    val userId = getCurrentUserId(call)
                    userStorage.getBySpotifyUserId(userId)?.copy(isLoggedIn = false, spotifyAccessToken = null, spotifyRefreshToken = null)
                        ?.apply { userStorage.save(this) }
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }

    private fun generateJWT(userId: String) = JWT.create()
        .withIssuer(PropertiesProvider.configuration.getProperty("jwtTokenIssuer"))
        .withSubject(PropertiesProvider.configuration.getProperty("jwtTokenSubject"))
        .withClaim("userId", userId)
        .withExpiresAt(Instant.now(Clock.systemUTC()).plusSeconds(60))
        .sign(Algorithm.HMAC256(PropertiesProvider.configuration.getProperty("jwtTokenSecret")))

    private fun generateRefreshToken(userId: String) = JWT.create()
        .withIssuer(PropertiesProvider.configuration.getProperty("jwtTokenIssuer"))
        .withSubject(PropertiesProvider.configuration.getProperty("jwtTokenSubject"))
        .withClaim("userId", userId)
        .withExpiresAt(Instant.now(Clock.systemUTC()).plusSeconds(60*60*24*7))
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

data class LoginStatusResponseDto(
    val loggedIn: Boolean,
    val name: String
)

data class RefreshTokenResponseDto(
    val token: String
)