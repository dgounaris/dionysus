package dgounaris.dionysus.server

import io.ktor.application.*

interface PlaylistsController {
    fun configureRouting(application: Application)
}