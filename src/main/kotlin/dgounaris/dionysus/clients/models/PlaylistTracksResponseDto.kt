package dgounaris.dionysus.clients.models

data class PlaylistTracksResponseDto(
    val items: List<TrackItem>
)

data class TrackItem(
    val track: TrackDetails
)

data class TrackDetails(
    val name: String,
    val id: String
)