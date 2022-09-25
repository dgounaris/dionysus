package dgounaris.dionysus.playback.models

import dgounaris.dionysus.tracks.models.TrackSections

data class PlaybackPlanItem(
    val user: String,
    val scheduledExecutionEpochTime: Long,
    val trackSections: TrackSections,
    val playbackDetails: PlaybackDetails
)