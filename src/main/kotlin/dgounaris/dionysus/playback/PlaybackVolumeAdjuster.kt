package dgounaris.dionysus.playback

interface PlaybackVolumeAdjuster {
    fun getFadeMilliseconds(): Int
    fun fadeOut(baselineVolume: Int)
    fun prepareFadeIn(baselineVolume: Int)
    fun fadeIn(baselineVolume: Int)
}