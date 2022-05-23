package dgounaris.dionysus.auth

import dgounaris.dionysus.user.models.User
import io.ktor.application.*

interface AuthorizationController {
    fun configureRouting(application: Application)

    fun getCurrentUser(): User?
}