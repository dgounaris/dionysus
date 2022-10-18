package dgounaris.dionysus.playback.models

data class PlaybackDetails(
    val selectedDeviceId: String,
    val selectedDeviceType: String,
    val selectedDeviceVolumePercent: Int,
    val fadeDetails: FadeDetails?
)

data class FadeDetails(
    val fadeMilliseconds: Int,
    val volumeChangeIntervalMilliseconds: Int,
    val volumeTotalReduction: Int
)