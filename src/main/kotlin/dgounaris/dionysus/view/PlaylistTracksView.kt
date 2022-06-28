package dgounaris.dionysus.view

import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.server.PlaylistResponseDto
import kotlinx.html.*

fun playlistTracksView(html: HTML, playlist: PlaylistResponseDto, availablePlaybackDevices: List<AvailableDevice>) {
    html.body {
        p {
            +"Playlist ${playlist.name}"
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
                playlist.trackDetails.map { trackDetails ->
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