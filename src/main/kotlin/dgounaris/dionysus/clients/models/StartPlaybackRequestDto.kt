package dgounaris.dionysus.clients.models

data class StartPlaybackRequestDto(
    val context_uri: String,
    val offset: Offset,
    val position_ms: Int
)

data class Offset(
    val uri: String
)