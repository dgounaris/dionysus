package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.*
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.min

class SectionMergingPlaybackOrchestrator(
    private val spotifyClient: SpotifyClient,
    private val playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy,
    private val playbackPlanMediator: PlaybackPlanMediator,
    private val playbackExecutor: PlaybackExecutor
    ) : PlaybackOrchestrator {

    override fun getAvailableDevices() : List<AvailableDevice> {
        val availableDevices = spotifyClient.getAvailableDevices()
        return availableDevices.devices
            .map { device -> AvailableDevice(device.id, device.name, device.type, device.is_active, device.volume_percent) }
            .toList()
    }

    override fun play(userId: String, tracksSections: List<TrackSections>, playbackDetails: PlaybackDetails) {
        val mergedTrackSectionsList = tracksSections.map { findSongSectionsToPlay(it) }
        val currentTime = System.currentTimeMillis()
        var delayFromPrevious = 0.0
        mergedTrackSectionsList.map {
            val item = PlaybackPlanItem(userId, currentTime + (delayFromPrevious * 1000).toLong(), it, playbackDetails)
            delayFromPrevious += (it.sections.lastOrNull()?.end ?: 0.0) - (it.sections.firstOrNull()?.start ?: 0.0)
            return@map item
        }.forEach {
            playbackPlanMediator.save(it)
        }
        runBlocking { playbackExecutor.play(userId, playbackPlanMediator, playbackVolumeAdjusterStrategy) }
    }

    override fun onPauseEvent(userId: String) {
        playbackPlanMediator.save(PlaybackEvent(userId, PlaybackEventType.PAUSE))
    }

    override fun onResumeEvent(userId: String) {
        playbackPlanMediator.save(PlaybackEvent(userId, PlaybackEventType.RESUME))
    }

    override fun onStopEvent(userId: String) {
        playbackPlanMediator.save(PlaybackEvent(userId, PlaybackEventType.STOP))
    }

    override fun onNextEvent(userId: String) {
        playbackPlanMediator.save(PlaybackEvent(userId, PlaybackEventType.NEXT))
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