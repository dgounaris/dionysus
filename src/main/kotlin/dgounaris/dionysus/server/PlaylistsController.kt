package dgounaris.dionysus.server

import io.ktor.server.application.*

interface PlaylistsController {
    fun configureRouting(application: Application)
}