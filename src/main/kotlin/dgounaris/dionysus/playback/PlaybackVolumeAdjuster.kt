package dgounaris.dionysus.playback

interface PlaybackVolumeAdjuster {
    fun getFadeMilliseconds(): Int
    fun fadeOut(userId: String, baselineVolume: Int)
    fun prepareFadeIn(userId: String, baselineVolume: Int)
    fun fadeIn(userId: String, baselineVolume: Int)
}