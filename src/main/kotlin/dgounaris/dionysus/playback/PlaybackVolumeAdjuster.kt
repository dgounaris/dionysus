package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.FadeDetails

interface PlaybackVolumeAdjuster {
    fun getFadeMilliseconds(): Int
    suspend fun fadeOut(userId: String, baselineVolume: Int, fadeDetails: FadeDetails? = null)
    suspend fun fadeIn(userId: String, baselineVolume: Int, fadeDetails: FadeDetails? = null)
}