package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playback.models.PlaybackEvent
import dgounaris.dionysus.tracks.models.TrackSections

interface PlaybackExecutor {
    suspend fun playSongSections(
        trackSections: TrackSections,
        playbackDetails: PlaybackDetails
    )

    suspend fun play(user: String)

    fun handleEvent(playbackEvent: PlaybackEvent)
}