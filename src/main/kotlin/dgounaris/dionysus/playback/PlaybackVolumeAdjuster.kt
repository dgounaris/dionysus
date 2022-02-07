package dgounaris.dionysus.playback

interface PlaybackVolumeAdjuster {
    fun fadeOut(baselineVolume: Int)
    fun prepareFadeIn(baselineVolume: Int)
    fun fadeIn(baselineVolume: Int)
}