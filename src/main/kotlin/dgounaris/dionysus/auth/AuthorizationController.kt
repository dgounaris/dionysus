package dgounaris.dionysus.auth

import io.ktor.server.application.*

interface AuthorizationController {
    fun configureRouting(application: Application)
    fun getCurrentUserId(call: ApplicationCall): String
}