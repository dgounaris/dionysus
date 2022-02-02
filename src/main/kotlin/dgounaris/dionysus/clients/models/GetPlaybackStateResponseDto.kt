package dgounaris.dionysus.clients.models

data class GetPlaybackStateResponseDto(
    val device: PlaybackStateResponseDevice,
    val item: PlaybackStateResponseItem
)

data class PlaybackStateResponseDevice(
    val id: String,
    val is_active: Boolean,
    val volume_percent: Int
)

data class PlaybackStateResponseItem(
    val id: String
)