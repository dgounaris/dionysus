package dgounaris.dionysus.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.common.PropertiesProvider
import dgounaris.dionysus.storage.user.UserStorage
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
                call.respondRedirect(spotifyClient.getAuthorizeUrl())
            }
            get("/callback") {
                val userId = callback(call.request.queryParameters["code"]!!, call.request.queryParameters["state"])
                val jwtToken = generateJWT(userId)
                call.respondRedirect(PropertiesProvider.configuration.getProperty("spotifyCallbackUrl") + "?token=" + jwtToken, false)
            }
        }
    }

    fun generateJWT(userId: String) = JWT.create()
        .withIssuer(PropertiesProvider.configuration.getProperty("jwtTokenIssuer"))
        .withSubject(PropertiesProvider.configuration.getProperty("jwtTokenSubject"))
        .withClaim("userId", userId)
        .sign(Algorithm.HMAC256(PropertiesProvider.configuration.getProperty("jwtTokenSecret")))

    override fun isAuthorized(user: String): Boolean {
        return userStorage.getBySpotifyUserId("") != null
    }

    override fun getCurrentUserId(): String = ""

    override fun getCurrentUserId(call: ApplicationCall): String {
        val principal = call.principal<JWTPrincipal>()
        return principal!!.payload.getClaim("userId").asString()
    }

    private fun callback(code: String, state: String?) : String =
        spotifyClient.getTokens(code)
}