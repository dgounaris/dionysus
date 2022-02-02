package dgounaris.dionysus.dionysense

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.tracks.TrackDetailsProvider
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileWriter
import java.util.*

class FeedbackHandlerImpl(
    private val trackDetailsProvider: TrackDetailsProvider,
    private val spotifyClient: SpotifyClient
    ) : FeedbackHandler {

    override fun handleFeedback() {
        val playbackState = spotifyClient.getPlaybackState()
        val trackDetails = runBlocking {
            trackDetailsProvider.getTrackDetails(playbackState.item.id)
        }
        try {
            val fileName = ".\\dionysus_feedback\\${playbackState.item.id}-${UUID.randomUUID()}.txt"
            val file = File(fileName)
            file.createNewFile()
            FileWriter(fileName).use {
                it.write(
                    """
                    Track id: ${trackDetails.id}
                    Track name: ${trackDetails.name}
                    Track analysis: ${trackDetails.sections}
                """.trimIndent()
                )
            }
        } catch (e: Exception) {
            println("Error while processing feedback: ${e.message}")
        }
    }
}