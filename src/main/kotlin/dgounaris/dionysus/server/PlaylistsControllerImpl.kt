package dgounaris.dionysus.server

import dgounaris.dionysus.common.parallelMap
import dgounaris.dionysus.playback.PlaybackHandler
import dgounaris.dionysus.playlists.PlaylistDetailsProvider
import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackDetails
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.*

class PlaylistsControllerImpl(
    private var playlistDetailsProvider: PlaylistDetailsProvider,
    private val playbackHandler: PlaybackHandler,
    private val trackDetailsProvider: TrackDetailsProvider
    ) : PlaylistsController {
    override fun configureRouting(application: Application) {
        application.routing {
            get("/playlists/tracks") {
                val playlistId = call.request.queryParameters["playlistName"]
                    ?: call.receiveParameters()["playlistName"]!!
                call.respondHtml { getPlaylistTracks(playlistId, this) }
            }
            get("/playlists/me") {
                call.respondHtml { getCurrentUserPlaylists(this) }
            }
        }
    }

    private fun getPlaylistTracks(playlistName: String, html: HTML) {
        val playlist = playlistDetailsProvider.getPlaylistDetails(playlistName)
        val playlistTrackDetails = runBlocking {
            playlist.tracks.parallelMap { track ->
                val trackDetails = trackDetailsProvider.getTrackDetails(track.id)
                trackDetails.toTrackDetailsResponseDto()
            }
        }
        val playlistResponse = PlaylistResponseDto(
            playlist.name, playlist.id, playlistTrackDetails
        )
        val availablePlaybackDevices = playbackHandler.getAvailableDevices()
        html.body {
            p {
                +"Playlist $playlistName"
                br
                +"The following tracks will be played:"
            }
            form {
                method = FormMethod.post
                input {
                    hidden = true
                    type = InputType.hidden
                    name = "playlistId_${playlist.id}"
                    id = "playlistId_${playlist.id}"
                    value = playlist.name
                    hidden
                }
                ol {
                    playlistResponse.trackDetails.map { trackDetails ->
                        li {
                            +trackDetails.name
                        }
                        br
                    }
                }
                label {
                    htmlFor = "devices_select"
                    text("Choose a playback device:")
                }
                select {
                    name = "device_select"
                    id = "devices_select"
                    availablePlaybackDevices.map {
                        option {
                            value = "${it.id}-${it.type}-${it.volumePercent}"
                            + ("${it.name} (${it.type})")
                        }
                    }
                }
                br
                input {
                    type = InputType.submit
                    value = "Autoplay using Dionysense for song and section order"
                    formAction = "http://localhost:8888/playback/play/auto"
                }
            }
        }
    }

    private fun getCurrentUserPlaylists(html: HTML) {
        val currentUserPlaylists = playlistDetailsProvider.getCurrentUserPlaylistNames()
        html.body {
            form {
                action = "http://localhost:8888/playlists/tracks"
                method = FormMethod.get
                label {
                    htmlFor = "playlists"
                    +"Choose a playlist:"
                }
                select {
                    id = "playlistName"
                    name = "playlistName"
                    currentUserPlaylists.map { playlistName ->
                        option {
                            value = playlistName
                            +playlistName
                        }
                    }
                }
                input {
                    type = InputType.submit
                    value = "Submit"
                }
            }
        }
    }
}

data class PlaylistResponseDto(
    val name: String,
    val id: String,
    val trackDetails: List<TrackDetailsResponseDto>
)

data class TrackDetailsResponseDto(
    val id: String,
    val name: String,
    val sections: List<TrackSectionResponseDto>
)

data class TrackSectionResponseDto(
    val start : Double,
    val end : Double,
    val tempo : Double,
    val key : Int,
    val mode : Int,
    val timeSignature : Int
)

fun TrackDetails.toTrackDetailsResponseDto() =
    TrackDetailsResponseDto(this.id, this.name, this.sections.map { section ->
        TrackSectionResponseDto(
            section.start,
            section.end,
            section.tempo,
            section.key,
            section.mode,
            section.timeSignature
        )
    })