package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.common.PropertiesProvider
import dgounaris.dionysus.playback.models.FadeDetails
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

class LinearPlaybackVolumeAdjuster(
    private val spotifyClient: SpotifyClient
    ) : PlaybackVolumeAdjuster {
    override suspend fun fadeOut(userId: String, baselineVolume: Int, fadeDetails: FadeDetails) {
        val selectedFadeMilliseconds = fadeDetails.fadeMilliseconds
        val selectedVolumeChangeIntervalMilliseconds = fadeDetails.volumeChangeIntervalMilliseconds
        val selectedVolumeReduction = fadeDetails.volumeTotalReduction
        var timesVolumeChanged = 0
        var currentVolume = baselineVolume
        val timesVolumeShouldChange = selectedFadeMilliseconds/selectedVolumeChangeIntervalMilliseconds
        while (timesVolumeChanged < timesVolumeShouldChange) {
            currentVolume -= selectedVolumeReduction/timesVolumeShouldChange
            spotifyClient.setVolume(userId, max(currentVolume, 0))
            delay(selectedVolumeChangeIntervalMilliseconds.toLong())
            timesVolumeChanged += 1
        }
    }

    override suspend fun fadeIn(userId: String, baselineVolume: Int, fadeDetails: FadeDetails) {
        val selectedFadeMilliseconds = fadeDetails.fadeMilliseconds
        val selectedVolumeChangeIntervalMilliseconds = fadeDetails.volumeChangeIntervalMilliseconds
        val selectedVolumeReduction = fadeDetails.volumeTotalReduction
        val startVolume = max(baselineVolume - fadeDetails.volumeTotalReduction, 0)
        var timesVolumeChanged = 0
        var currentVolume = startVolume
        while (timesVolumeChanged < selectedFadeMilliseconds/selectedVolumeChangeIntervalMilliseconds) {
            currentVolume += (selectedVolumeReduction/(selectedFadeMilliseconds/selectedVolumeChangeIntervalMilliseconds))
            spotifyClient.setVolume(userId, min(currentVolume, 100))
            delay(selectedVolumeChangeIntervalMilliseconds.toLong())
            timesVolumeChanged += 1
        }
    }
}