package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.tracks.models.TrackSection
import dgounaris.dionysus.tracks.models.TrackSectionStartEnd
import dgounaris.dionysus.tracks.models.TrackSections
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class PlaybackHandlerImpl(
    private val spotifyClient: SpotifyClient,
    private val playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy
    ) : PlaybackHandler {

    override fun getAvailableDevices() : List<AvailableDevice> {
        val availableDevices = spotifyClient.getAvailableDevices()
        return availableDevices.devices
            .map { device -> AvailableDevice(device.id, device.name, device.type, device.is_active, device.volume_percent) }
            .toList()
    }

    override fun play(playlistId: String, tracksSections: List<TrackSections>, playbackDetails: PlaybackDetails) {
        val mergedTrackSectionsList = tracksSections.map { findSongSectionsToPlay(it) }
        mergedTrackSectionsList.forEach { mergedTrackSections ->
            playSongSections(playlistId, mergedTrackSections, playbackVolumeAdjusterStrategy, playbackDetails)
        }
    }

    private fun playSongSections(
        playlistId: String,
        trackSections: TrackSections,
        playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy,
        playbackDetails: PlaybackDetails
    ) {
        val playbackVolumeAdjuster = playbackVolumeAdjusterStrategy.getVolumeAdjuster(playbackDetails)
        val fadeMilliseconds = playbackVolumeAdjuster.getFadeMilliseconds()
        trackSections.sections.firstOrNull()?.apply {
            val effectiveStartTime = max((this@apply.start * 1000).toInt() - fadeMilliseconds, 0)
            playbackVolumeAdjuster.prepareFadeIn(playbackDetails.selectedDeviceVolumePercent)
            spotifyClient.playPlaylistTrack(playlistId, trackSections.id, playbackDetails.selectedDeviceId, effectiveStartTime)
            playbackVolumeAdjuster.fadeIn(playbackDetails.selectedDeviceVolumePercent)
            runBlocking {
                delay((this@apply.end * 1000 - this@apply.start * 1000).roundToLong())
            }
        }
        trackSections.sections.drop(1).forEach { sectionToPlay ->
            spotifyClient.seekPlaybackPosition((sectionToPlay.start * 1000).roundToInt())
            runBlocking {
                delay((sectionToPlay.end * 1000 - sectionToPlay.start * 1000 - fadeMilliseconds).roundToLong())
            }
        }
        playbackVolumeAdjuster.fadeOut(playbackDetails.selectedDeviceVolumePercent)
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