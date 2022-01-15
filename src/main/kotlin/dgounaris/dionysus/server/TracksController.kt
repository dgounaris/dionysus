package dgounaris.dionysus.server

import io.ktor.application.*

interface TracksController {
    fun configureRouting(application: Application)
}