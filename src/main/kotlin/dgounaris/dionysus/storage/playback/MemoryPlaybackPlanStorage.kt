package dgounaris.dionysus.storage.playback

import dgounaris.dionysus.playback.models.PlaybackPlan

class MemoryPlaybackPlanStorage : PlaybackPlanStorage {
    private val playbackPlanStorage: HashMap<String, PlaybackPlan> = hashMapOf()
    override fun save(playbackPlan: PlaybackPlan) {
        // todo don't overwrite previous playback plans for the same user
        playbackPlanStorage[playbackPlan.userId] = playbackPlan
    }
}