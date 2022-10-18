package dgounaris.dionysus.auth

import io.ktor.server.application.*

interface AuthorizationController {
    fun configureRouting(application: Application)
    fun getCurrentUserId(): String
    fun getCurrentUserId(call: ApplicationCall): String
    fun isAuthorized(user: String): Boolean
}