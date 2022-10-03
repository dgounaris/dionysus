package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playback.models.PlaybackEvent
import dgounaris.dionysus.playback.models.PlaybackEventType
import dgounaris.dionysus.tracks.models.TrackSections
import dgounaris.dionysus.user.models.User

interface PlaybackOrchestrator {
    fun getAvailableDevices(userId: String) : List<AvailableDevice>
    fun play(userId: String, tracksSections: List<TrackSections>, playbackDetails: PlaybackDetails)
    fun onPauseEvent(userId: String)
    fun onResumeEvent(userId: String)
    fun onStopEvent(userId: String)
    fun onNextEvent(userId: String)
}