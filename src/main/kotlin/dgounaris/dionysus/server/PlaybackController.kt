package dgounaris.dionysus.server

import io.ktor.server.application.*

interface PlaybackController {
    fun configureRouting(application: Application)
}