package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackPlanItem

interface PlaybackPlan {

}

class SimplePlaybackPlan : PlaybackPlan {
    private val queue = mutableMapOf<String, PlaybackPlanItem>()
}