package dgounaris.dionysus.playback

import dgounaris.dionysus.tracks.models.TrackSections

interface PlaybackHandler {
    fun play(playlistId: String, tracksSections: List<TrackSections>)
}