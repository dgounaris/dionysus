package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackEvent
import kotlin.properties.Delegates

interface PlaybackEventHandler {
    fun pushEvent(playbackEvent: PlaybackEvent)
}

class SimplePlaybackEventHandler(private val playbackExecutor: PlaybackExecutor) : PlaybackEventHandler {
    private var event : PlaybackEvent? by Delegates.observable(
        null
    ) {
            _, _, newEvent -> playbackExecutor.handleEvent(newEvent!!)
    }

    override fun pushEvent(playbackEvent: PlaybackEvent) {
        synchronized(this) {
            event = playbackEvent
        }
    }
}