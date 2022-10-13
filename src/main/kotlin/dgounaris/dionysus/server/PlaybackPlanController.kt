package dgounaris.dionysus.server

import io.ktor.application.*

interface PlaybackPlanController {
    fun configureRouting(application: Application)
}