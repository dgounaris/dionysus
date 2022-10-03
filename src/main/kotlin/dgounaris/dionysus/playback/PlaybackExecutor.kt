package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackEvent

interface PlaybackExecutor {
    suspend fun handleEvent(playbackEvent: PlaybackEvent)
}