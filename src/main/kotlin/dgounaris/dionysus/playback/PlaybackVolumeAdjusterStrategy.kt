package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.PlaybackDetails

interface PlaybackVolumeAdjusterStrategy {
    fun getVolumeAdjuster(playbackDetails: PlaybackDetails): PlaybackVolumeAdjuster
}

class PlaybackVolumeAdjusterStrategyImpl(private val spotifyClient: SpotifyClient) : PlaybackVolumeAdjusterStrategy {
    override fun getVolumeAdjuster(playbackDetails: PlaybackDetails): PlaybackVolumeAdjuster {
        return if (playbackDetails.selectedDeviceType.equals("SMARTPHONE", true)) {
            NoopPlaybackVolumeAdjuster()
        } else {
            LinearPlaybackVolumeAdjuster(spotifyClient)
        }
    }
}