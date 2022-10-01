package dgounaris.dionysus.playback.models

data class PlaybackEvent(
    val user: String,
    val eventType: PlaybackEventType
)

enum class PlaybackEventType {
    PAUSE,
    STOP,
    RESUME
}