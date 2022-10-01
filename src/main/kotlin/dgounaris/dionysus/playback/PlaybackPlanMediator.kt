package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackPlanItem
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.PriorityBlockingQueue

interface PlaybackPlanMediator {
    fun save(playbackPlanItem: PlaybackPlanItem)
    fun getNextPlanItem(user: String) : PlaybackPlanItem?
}

class SimplePlaybackPlanMediator : PlaybackPlanMediator {
    private val pq = ConcurrentHashMap<String, ConcurrentLinkedQueue<PlaybackPlanItem>>()

    override fun save(playbackPlanItem: PlaybackPlanItem) {
        val userPq = pq.getOrDefault(playbackPlanItem.user, ConcurrentLinkedQueue())
        userPq.add(playbackPlanItem)
        val putResult = pq.putIfAbsent(playbackPlanItem.user, userPq)
        if (putResult != null) {
            pq.replace(playbackPlanItem.user, userPq)
        }
    }

    override fun getNextPlanItem(user: String) : PlaybackPlanItem? {
        return pq[user]?.poll()
    }
}