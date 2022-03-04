package dgounaris.dionysus.playback.models

data class PlaybackDetails(
    val selectedDeviceId: String,
    val selectedDeviceType: String,
    val selectedDeviceVolumePercent: Int
)