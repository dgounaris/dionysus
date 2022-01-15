package dgounaris.dionysus.server

import dgounaris.dionysus.clients.SpotifyClient
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

class TracksControllerImpl(private var spotifyClient: SpotifyClient): TracksController {
    override fun configureRouting(application: Application) {
        application.routing {
            get("/tracks/analysis") {
                val trackId = call.request.queryParameters["trackId"]!!
                val response = analyzeTrack(trackId)
                call.respondText { response.toString() }
            }
        }
    }

    private fun analyzeTrack(trackId: String) =
        spotifyClient.getTrackAudioAnalysis(trackId)

}