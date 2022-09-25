package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackPlanItem
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

interface PlaybackPlanMediator {
    fun save(playbackPlanItem: PlaybackPlanItem)
    //fun getNextPlanItem(user: String)
    //fun delayNextPlanItem(user: String) // this can be implemented by mutating the next playback's exec time
}

class SimplePlaybackPlanMediator : PlaybackPlanMediator {
    private val playbackPlanItemComparator: Comparator<PlaybackPlanItem> = compareBy { it.scheduledExecutionEpochTime }
    private val pq = ConcurrentHashMap<String, PriorityBlockingQueue<PlaybackPlanItem>>()

    override fun save(playbackPlanItem: PlaybackPlanItem) {
        val userPq = pq.getOrDefault(playbackPlanItem.user, PriorityBlockingQueue(50, playbackPlanItemComparator))
        userPq.add(playbackPlanItem)
        pq.replace(playbackPlanItem.user, userPq)
    }

    /*override fun getNextPlanItem(user: String) {
        pq.remove()
    }

    override fun delayNextPlanItem(user: String) {
        val element = pq.remove()
    }*/
}