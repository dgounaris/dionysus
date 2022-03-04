package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.PlaybackState

class PlaybackStatusShortPollingService(private val spotifyClient: SpotifyClient) : PlaybackStatusPollingService {
    override fun pollPlaybackStatus(): PlaybackState? {
        val playbackState = spotifyClient.getPlaybackState() ?: return null
        return PlaybackState(
            playbackState.device.id,
            playbackState.device.name,
            playbackState.device.type,
            playbackState.device.volume_percent,
            playbackState.progress_ms,
            playbackState.is_playing,
            playbackState.item.id,
            playbackState.item.name
        )
    }
}