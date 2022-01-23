package dgounaris.dionysus.clients.models

data class GetPlaybackStateResponseDto(
    val item: PlaybackStateResponseItem
)

data class PlaybackStateResponseItem(
    val id: String
)