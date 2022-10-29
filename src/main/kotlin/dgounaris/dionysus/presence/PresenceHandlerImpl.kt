package dgounaris.dionysus.presence

import dgounaris.dionysus.playback.PlaybackOrchestrator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Clock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class PresenceHandlerImpl(
    playbackOrchestrator: PlaybackOrchestrator
) : PresenceHandler {

    init {
        thread {
            while (true) {
                presenceLatestProbe.toMap().forEach { (userId, instant) ->
                    if (instant < Instant.now(Clock.systemUTC()).minusSeconds(10)) {
                        playbackOrchestrator.onStopEvent(userId)
                        presenceLatestProbe.remove(userId)
                    }
                }
                runBlocking { delay(10000) }
            }
        }
    }

    private val presenceLatestProbe = ConcurrentHashMap<String, Instant>()

    override fun savePresenceProbe(userId: String) {
        presenceLatestProbe[userId] = Instant.now(Clock.systemUTC())
    }
}