package dgounaris.dionysus.storage.playback

import dgounaris.dionysus.playback.models.PlaybackPlan
import dgounaris.dionysus.tracks.models.Track

interface PlaybackPlanStorage {
    fun save(playbackPlan: PlaybackPlan)
}