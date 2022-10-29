package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playback.models.PlaybackState
import dgounaris.dionysus.tracks.models.TrackSection

interface PlaybackOrchestrator {
    fun getAvailableDevices(userId: String) : List<AvailableDevice>
    fun pushPlaybackPlanItem(userId: String, trackId: String, trackSections: List<TrackSection>)
    fun play(userId: String, playbackDetails: PlaybackDetails)
    fun updateVolume(userId: String, volumePercent: Int)
    fun getCurrentState(userId: String): PlaybackState
    fun onPauseEvent(userId: String)
    fun onResumeEvent(userId: String)
    fun onStopEvent(userId: String)
    fun onNextEvent(userId: String)
}