package dgounaris.dionysus.server

import io.ktor.application.*

interface PlaybackController {
    fun configureRouting(application: Application)
}