package dgounaris.dionysus.server

import io.ktor.application.*

interface AuthorizationController {
    fun configureRouting(application: Application)
}