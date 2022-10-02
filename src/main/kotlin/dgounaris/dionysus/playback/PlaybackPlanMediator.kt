package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackPlanItem
import java.util.concurrent.*

interface PlaybackPlanMediator {
    fun save(playbackPlanItem: PlaybackPlanItem)
    fun getNextPlanItem(user: String) : PlaybackPlanItem?
}

class SimplePlaybackPlanMediator : PlaybackPlanMediator {
    private val playbackQueue = ConcurrentHashMap<String, ConcurrentLinkedQueue<PlaybackPlanItem>>()

    override fun save(playbackPlanItem: PlaybackPlanItem) {
        val userQueue = playbackQueue.getOrDefault(playbackPlanItem.user, ConcurrentLinkedQueue())
        userQueue.add(playbackPlanItem)
        val putResult = playbackQueue.putIfAbsent(playbackPlanItem.user, userQueue)
        if (putResult != null) {
            playbackQueue.replace(playbackPlanItem.user, userQueue)
        }
    }

    override fun getNextPlanItem(user: String) : PlaybackPlanItem? {
        return playbackQueue[user]?.poll()
    }
}