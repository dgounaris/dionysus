package dgounaris.dionysus.clients.models

data class GetPlaybackStateResponseDto(
    val device: PlaybackStateResponseDevice,
    val item: PlaybackStateResponseItem,
    val progress_ms: Int,
    val is_playing: Boolean
)

data class PlaybackStateResponseDevice(
    val id: String,
    val name: String,
    val type: String,
    val is_active: Boolean,
    val volume_percent: Int
)

data class PlaybackStateResponseItem(
    val id: String,
    val name: String
)