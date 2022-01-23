package dgounaris.dionysus.tracks.models

data class TrackDetails(
    val id: String,
    val name: String,
    val sections: List<TrackSection>
)

data class TrackSection(
    val start : Double,
    val duration: Double,
    val end : Double,
    val confidence: Double,
    val loudness: Double,
    val tempo : Double,
    val key : Int,
    val mode : Int,
    val timeSignature : Int
)