package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.PlaybackPlanItem

interface PlaybackPlanMediator {
    //fun save(user: String, playbackPlanItem: PlaybackPlanItem)
    //fun getNextPlanItem(user: String)
    //fun delayNextPlanItem(user: String) // this can be implemented by mutating the next playback's exec time
}

class SimplePlaybackPlanMediator : PlaybackPlanMediator {
    private val queue = mutableMapOf<String, Map<Long, PlaybackPlanItem>>() //<user, <epoch to play, planitem>>
}