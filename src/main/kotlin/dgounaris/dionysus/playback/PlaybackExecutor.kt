package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.tracks.models.TrackSections

interface PlaybackExecutor {
    fun playSongSections(
        trackSections: TrackSections,
        playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy,
        playbackDetails: PlaybackDetails
    )
}