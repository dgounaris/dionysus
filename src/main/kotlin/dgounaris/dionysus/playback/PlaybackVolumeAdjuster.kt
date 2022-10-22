package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.FadeDetails

interface PlaybackVolumeAdjuster {
    suspend fun fadeOut(userId: String, baselineVolume: Int, fadeDetails: FadeDetails)
    suspend fun fadeIn(userId: String, baselineVolume: Int, fadeDetails: FadeDetails)
}