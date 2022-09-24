package dgounaris.dionysus.server

import dgounaris.dionysus.auth.AuthorizationController
import dgounaris.dionysus.clients.SpotifyClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking

class TracksControllerImpl(
    private var spotifyClient: SpotifyClient,
    private val authorizationController: AuthorizationController
    ): TracksController {
    override fun configureRouting(application: Application) {
        application.routing {
            get("/tracks/analysis") {
                if (!authorizationController.isAuthorized("")) {
                    return@get call.respond(HttpStatusCode.Unauthorized)
                }
                val trackId = call.request.queryParameters["trackId"]!!
                val response = analyzeTrack(trackId)
                call.respondText { response.toString() }
            }
        }
    }

    private fun analyzeTrack(trackId: String) =
        runBlocking { spotifyClient.getTrackAudioAnalysis(trackId) }

}