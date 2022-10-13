package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playback.models.PlaybackPlanItem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import java.util.concurrent.*

interface PlaybackPlanMediator {
    fun savePlaybackPlanItem(playbackPlanItem: PlaybackPlanItem)
    fun saveActivePlaybackJob(userId: String, playbackJob: Job)
    fun getNextPlanItem(user: String) : PlaybackPlanItem?
    fun getActivePlaybackJob(userId: String) : Job?
    fun deleteActivePlaybackJob(userId: String)
    fun clearPlaybackPlanQueue(userId: String)
    fun setPlaybackDetails(userId: String, playbackDetails: PlaybackDetails)
    fun getPlaybackDetails(userId: String) : PlaybackDetails?
}

class SimplePlaybackPlanMediator : PlaybackPlanMediator {
    private val playbackQueue = ConcurrentHashMap<String, ConcurrentLinkedQueue<PlaybackPlanItem>>()
    private val activePlaybackJobs = ConcurrentHashMap<String, Job>()
    private val selectedPlaybackDetails = ConcurrentHashMap<String, PlaybackDetails>()

    override fun savePlaybackPlanItem(playbackPlanItem: PlaybackPlanItem) {
        val userQueue = playbackQueue.getOrDefault(playbackPlanItem.user, ConcurrentLinkedQueue())
        userQueue.add(playbackPlanItem)
        val putResult = playbackQueue.putIfAbsent(playbackPlanItem.user, userQueue)
        if (putResult != null) {
            playbackQueue.replace(playbackPlanItem.user, userQueue)
        }
    }

    override fun saveActivePlaybackJob(userId: String, playbackJob: Job) {
        activePlaybackJobs[userId] = playbackJob
    }

    override fun getActivePlaybackJob(userId: String) = activePlaybackJobs[userId]

    override fun deleteActivePlaybackJob(userId: String) {
        activePlaybackJobs[userId]?.cancel()
        activePlaybackJobs.remove(userId)
    }

    override fun getNextPlanItem(user: String) : PlaybackPlanItem? {
        return playbackQueue[user]?.poll()
    }

    override fun clearPlaybackPlanQueue(userId: String) {
        playbackQueue[userId]?.clear()
    }

    override fun setPlaybackDetails(userId: String, playbackDetails: PlaybackDetails) {
        selectedPlaybackDetails[userId] = playbackDetails
    }

    override fun getPlaybackDetails(userId: String) =
        selectedPlaybackDetails[userId]
}