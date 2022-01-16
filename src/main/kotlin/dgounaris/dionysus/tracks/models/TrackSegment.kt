package dgounaris.dionysus.tracks.models

data class TrackSections(
    val id: String,
    val sections: List<TrackSectionStartEnd>
)

data class TrackSectionStartEnd(
    val start: Double,
    val end: Double
)