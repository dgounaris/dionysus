package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackState

interface PlaybackStatusPollingService {
    fun pollPlaybackStatus(userId: String): PlaybackState?
}