package dgounaris.dionysus.playback.models

import dgounaris.dionysus.tracks.models.TrackSections

data class PlaybackPlanItem(
    val trackSections: TrackSections,
    val playbackDetails: PlaybackDetails
)