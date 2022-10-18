package dgounaris.dionysus.server

import io.ktor.server.application.*

interface PlaybackPlanController {
    fun configureRouting(application: Application)
}