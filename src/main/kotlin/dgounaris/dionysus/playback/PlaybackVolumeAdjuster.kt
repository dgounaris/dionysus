package dgounaris.dionysus.playback

interface PlaybackVolumeAdjuster {
    fun getFadeMilliseconds(): Int
    suspend fun fadeOut(userId: String, baselineVolume: Int)
    suspend fun fadeIn(userId: String, baselineVolume: Int)
}