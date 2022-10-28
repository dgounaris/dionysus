package dgounaris.dionysus.server

import io.ktor.server.application.*

interface StatePollingController {
    fun configureRouting(application: Application)
}