package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackEvent
import dgounaris.dionysus.playback.models.PlaybackPlanItem
import java.util.concurrent.*

interface PlaybackPlanMediator {
    fun save(playbackPlanItem: PlaybackPlanItem)
    fun save(playbackEvent: PlaybackEvent)
    fun getNextPlanItem(user: String) : PlaybackPlanItem?
}

class SimplePlaybackPlanMediator : PlaybackPlanMediator {
    private val playbackQueue = ConcurrentHashMap<String, ConcurrentLinkedQueue<PlaybackPlanItem>>()
    private val eventQueue = ConcurrentHashMap<String, BlockingQueue<PlaybackEvent>>()

    override fun save(playbackPlanItem: PlaybackPlanItem) {
        val userQueue = playbackQueue.getOrDefault(playbackPlanItem.user, ConcurrentLinkedQueue())
        userQueue.add(playbackPlanItem)
        val putResult = playbackQueue.putIfAbsent(playbackPlanItem.user, userQueue)
        if (putResult != null) {
            playbackQueue.replace(playbackPlanItem.user, userQueue)
        }
    }

    override fun save(playbackEvent: PlaybackEvent) {
        val userQueue = eventQueue.getOrDefault(playbackEvent.user, LinkedBlockingDeque())
        userQueue.add(playbackEvent)
        val putResult = eventQueue.putIfAbsent(playbackEvent.user, userQueue)
        if (putResult != null) {
            eventQueue.replace(playbackEvent.user, userQueue)
        }
    }

    override fun getNextPlanItem(user: String) : PlaybackPlanItem? {
        return playbackQueue[user]?.poll()
    }
}