package dgounaris.dionysus.playback.models

import dgounaris.dionysus.tracks.models.TrackSections

data class PlaybackPlanItem(
    val user: String,
    val trackSections: TrackSections
)