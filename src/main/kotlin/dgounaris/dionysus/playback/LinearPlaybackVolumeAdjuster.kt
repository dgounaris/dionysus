package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.min

class LinearPlaybackVolumeAdjuster(
    private val spotifyClient: SpotifyClient
    ) : PlaybackVolumeAdjuster {

    private val fadeMilliseconds = 150
    private val volumeChangeIntervalMilliseconds = 30
    private val volumeReduction = 30

    override fun getFadeMilliseconds(): Int = fadeMilliseconds

    override fun fadeOut(baselineVolume: Int) {
        runBlocking {
            var timesVolumeChanged = 0
            var currentVolume = baselineVolume
            val timesVolumeShouldChange = fadeMilliseconds/volumeChangeIntervalMilliseconds
            while (timesVolumeChanged < timesVolumeShouldChange) {
                currentVolume -= volumeReduction/timesVolumeShouldChange
                spotifyClient.setVolume(max(currentVolume, 0))
                delay(volumeChangeIntervalMilliseconds.toLong())
                timesVolumeChanged += 1
            }
        }
    }

    override fun prepareFadeIn(baselineVolume: Int) {
        val startVolume = max(baselineVolume - volumeReduction, 0)
        spotifyClient.setVolume(startVolume)
    }

    override fun fadeIn(baselineVolume: Int) {
        val startVolume = max(baselineVolume - volumeReduction, 0)
        runBlocking {
            var timesVolumeChanged = 0
            var currentVolume = startVolume
            while (timesVolumeChanged < fadeMilliseconds/volumeChangeIntervalMilliseconds) {
                currentVolume += (volumeReduction/(fadeMilliseconds/volumeChangeIntervalMilliseconds))
                spotifyClient.setVolume(min(currentVolume, 100))
                delay(volumeChangeIntervalMilliseconds.toLong())
                timesVolumeChanged += 1
            }
        }
    }
}