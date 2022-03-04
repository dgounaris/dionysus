package dgounaris.dionysus.tracks.models

data class TrackSections(
    val id: String,
    val sections: List<TrackSectionStartEnd>
)

data class TrackSectionStartEnd(
    var start: Double,
    var end: Double
)