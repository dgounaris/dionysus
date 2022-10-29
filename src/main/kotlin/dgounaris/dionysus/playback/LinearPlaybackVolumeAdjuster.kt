package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.FadeDetails
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class LinearPlaybackVolumeAdjuster(
    private val spotifyClient: SpotifyClient
    ) : PlaybackVolumeAdjuster {
    override suspend fun fadeOut(userId: String, baselineVolume: Int, fadeDetails: FadeDetails) {
        val finalVolume = baselineVolume * (100-fadeDetails.volumeTotalReduction) / 100
        val volumeStepReduction = ceil(
            fadeDetails.volumeTotalReduction.toDouble()/(fadeDetails.fadeMilliseconds/fadeDetails.volumeChangeIntervalMilliseconds)
        ).toInt()
        var currentVolume = baselineVolume
        while (currentVolume > finalVolume) {
            currentVolume =
                if (currentVolume-volumeStepReduction > finalVolume) {
                    currentVolume-volumeStepReduction
                } else {
                    finalVolume
                }
            spotifyClient.setVolume(userId, max(currentVolume, 0))
            delay(fadeDetails.volumeChangeIntervalMilliseconds.toLong())
        }
    }

    override suspend fun fadeIn(userId: String, baselineVolume: Int, fadeDetails: FadeDetails) {
        val finalVolume = baselineVolume
        val volumeStepIncrease = ceil(
            fadeDetails.volumeTotalReduction.toDouble()/(fadeDetails.fadeMilliseconds/fadeDetails.volumeChangeIntervalMilliseconds)
        ).toInt()
        var currentVolume = baselineVolume * (100-fadeDetails.volumeTotalReduction) / 100
        while (currentVolume < finalVolume) {
            currentVolume =
                if (currentVolume+volumeStepIncrease < finalVolume) {
                    currentVolume+volumeStepIncrease
                } else {
                    finalVolume
                }
            spotifyClient.setVolume(userId, max(currentVolume, 0))
            delay(fadeDetails.volumeChangeIntervalMilliseconds.toLong())
        }
    }
}