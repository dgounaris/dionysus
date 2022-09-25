package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.tracks.models.TrackSections

interface PlaybackExecutor {
    fun playSongSections(
        trackSections: TrackSections,
        playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy,
        playbackDetails: PlaybackDetails
    )

    suspend fun play(user: String, playbackPlanMediator: PlaybackPlanMediator, playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy)
}