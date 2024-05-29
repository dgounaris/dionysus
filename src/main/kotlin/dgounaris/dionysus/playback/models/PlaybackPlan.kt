package dgounaris.dionysus.playback.models

import dgounaris.dionysus.dionysense.models.SectionSelectionOptions
import dgounaris.dionysus.server.TrackSelections
import dgounaris.dionysus.tracks.models.TrackDetails

data class PlaybackPlan(
    val userId: String,
    val trackDetails: List<TrackDetails>,
    val selections: List<TrackSelections>,
    val selectionOptions: SectionSelectionOptions
)