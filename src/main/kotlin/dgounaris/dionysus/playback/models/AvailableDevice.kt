package dgounaris.dionysus.playback.models

data class AvailableDevice(
    val id: String,
    val name: String,
    val type: String,
    val isActive: Boolean,
    val volumePercent: Int
)
