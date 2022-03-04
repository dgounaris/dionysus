package dgounaris.dionysus.playback.models

data class PlaybackState(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val volumePercent: Int,
    val progressMs: Int,
    val isPlaying: Boolean,
    val playingItemId: String,
    val playingItemName: String
)