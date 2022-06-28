package dgounaris.dionysus.server

import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.dionysense.FeedbackHandler
import dgounaris.dionysus.dionysense.TrackOrderSelector
import dgounaris.dionysus.dionysense.TrackSectionSelector
import dgounaris.dionysus.playback.PlaybackHandler
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import dgounaris.dionysus.view.postAutoplayView
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlin.concurrent.thread

class PlaybackControllerImpl(
    private val playlistDetailsProvider: PlaylistDetailsProvider,
    private val trackSectionSelector: TrackSectionSelector,
    private val trackOrderSelector: TrackOrderSelector,
    private val playbackHandler: PlaybackHandler,
    private val feedbackHandler: FeedbackHandler
    ): PlaybackController {
    override fun configureRouting(application: Application) {
        application.routing {
            post("/playback/play/auto") {
                val formParameters = call.receiveParameters()
                thread { autoplay(formParameters) }
                call.respondHtml { responseAutoplayStartedOk(this) }
            }
            post("/playback/feedback") {
                submitFeedback()
            }
        }
    }

    private fun autoplay(params: Parameters) {
        val targetDeviceDetails = params.entries().single { it.key == "device_select" }.value.single()
        val playbackDetails = PlaybackDetails(
            targetDeviceDetails.split("-")[0],
            targetDeviceDetails.split("-")[1],
            targetDeviceDetails.split("-")[2].toInt()
        )
        val playlistName = params.entries().single { it.key.startsWith("playlistId_") }.value.single()
        val targetPlaylist = playlistDetailsProvider.getPlaylistDetails(playlistName)
        val targetTracksWithCustomOrder = runBlocking { trackOrderSelector.selectOrder(targetPlaylist.tracks.map { it.id }) }
        val targetSections = runBlocking {
            targetTracksWithCustomOrder
                .parallelMap { trackId ->
                    val sections = trackSectionSelector.selectSections(trackId)
                    TrackSections(trackId, sections.map { section -> TrackSectionStartEnd(section.start, section.end) })
                }
        }
        playbackHandler.play(targetSections, playbackDetails)
    }

    private fun submitFeedback() {
        feedbackHandler.handleFeedback()
    }

    private fun responseAutoplayStartedOk(html: HTML) {
        postAutoplayView(html)
    }
}