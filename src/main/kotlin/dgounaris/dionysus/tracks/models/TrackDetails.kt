package dgounaris.dionysus.tracks.models

data class TrackDetails(
    val id: String,
    val name: String,
    val sections: List<TrackSection>,
    val features: TrackAudioFeatures?
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

data class TrackAudioFeatures(
    val acousticness: Double,
    val danceability: Double,
    val energy: Double,
    val instrumentalness: Double,
    val key: Int,
    val liveness: Double,
    val loudness: Double,
    val speechiness: Double,
    val tempo: Double,
    val timeSignature: Int,
    val valence: Double
)