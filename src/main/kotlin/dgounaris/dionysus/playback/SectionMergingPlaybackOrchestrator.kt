package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.*
import dgounaris.dionysus.tracks.models.TrackSection
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import kotlin.math.max
import kotlin.math.min

class SectionMergingPlaybackOrchestrator(
    private val spotifyClient: SpotifyClient,
    private val playbackPlanMediator: PlaybackPlanMediator,
    private val playbackEventHandler: PlaybackEventHandler
    ) : PlaybackOrchestrator {

    override fun getAvailableDevices(userId: String) : List<AvailableDevice> {
        val availableDevices = spotifyClient.getAvailableDevices(userId)
        return availableDevices.devices
            .map { device -> AvailableDevice(device.id, device.name, device.type, device.is_active, device.volume_percent) }
            .toList()
    }

    override fun pushPlaybackPlanItem(userId: String, trackId: String, trackSections: List<TrackSection>) {
        val finalTrackSections = TrackSections(trackId, trackSections.map { section -> TrackSectionStartEnd(section.start, section.end) })
        val mergedTrackSectionsList = findSongSectionsToPlay(finalTrackSections)
        val item = PlaybackPlanItem(userId, mergedTrackSectionsList)
        playbackPlanMediator.savePlaybackPlanItem(item)
    }

    override fun play(userId: String, playbackDetails: PlaybackDetails) {
        playbackPlanMediator.setPlaybackDetails(userId, playbackDetails)
        playbackEventHandler.pushEvent(PlaybackEvent(userId, PlaybackEventType.START))
    }

    override fun onPauseEvent(userId: String) {
        playbackEventHandler.pushEvent(PlaybackEvent(userId, PlaybackEventType.PAUSE))
    }

    override fun onResumeEvent(userId: String) {
        playbackEventHandler.pushEvent(PlaybackEvent(userId, PlaybackEventType.RESUME))
    }

    override fun onStopEvent(userId: String) {
        playbackEventHandler.pushEvent(PlaybackEvent(userId, PlaybackEventType.STOP))
    }

    override fun onNextEvent(userId: String) {
        playbackEventHandler.pushEvent(PlaybackEvent(userId, PlaybackEventType.NEXT))
    }

    private fun findSongSectionsToPlay(trackSections: TrackSections): TrackSections {
        val distinctOrderedSections = trackSections.sections
            .sortedBy { section -> section.start }
            .map { section -> TrackSectionStartEnd(
                section.start, section.end
            ) }
        return TrackSections(trackSections.id, mergeAdjacentSections(distinctOrderedSections))
    }

    private fun mergeAdjacentSections(sections: List<TrackSectionStartEnd>): List<TrackSectionStartEnd> {
        var previousSection = sections.firstOrNull() ?: return emptyList()
        val finalSectionList = mutableListOf(previousSection)
        for(index in 1 until sections.size) {
            if (previousSection.end >= sections[index].start - 0.1) { // -0.1 to tolerate arithmetic inaccuracies
                previousSection.start = min(previousSection.start, sections[index].start)
                previousSection.end = max(previousSection.end, sections[index].end)
            } else {
                finalSectionList.add(sections[index])
                previousSection = finalSectionList.last()
            }
        }
        return finalSectionList
    }
}