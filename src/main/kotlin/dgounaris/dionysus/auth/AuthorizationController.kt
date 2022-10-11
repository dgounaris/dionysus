package dgounaris.dionysus.auth

import dgounaris.dionysus.user.models.User
import io.ktor.application.*

interface AuthorizationController {
    fun configureRouting(application: Application)
    fun getCurrentUserId(): String
    fun getCurrentUserId(call: ApplicationCall): String
    fun isAuthorized(user: String): Boolean
}