package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.tracks.models.TrackSections

interface PlaybackHandler {
    fun getAvailableDevices() : List<AvailableDevice>
    fun play(playlistId: String, tracksSections: List<TrackSections>, playbackDetails: PlaybackDetails)
}