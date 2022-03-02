package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.AvailableDevice
import dgounaris.dionysus.tracks.models.TrackSections
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class PlaybackHandlerImpl(
    private val spotifyClient: SpotifyClient,
    private val playbackVolumeAdjuster: PlaybackVolumeAdjuster
    ) : PlaybackHandler {

    private val fadeMilliseconds = playbackVolumeAdjuster.getFadeMilliseconds()

    override fun getAvailableDevices() : List<AvailableDevice> {
        val availableDevices = spotifyClient.getAvailableDevices()
        return availableDevices.devices
            .map { device -> AvailableDevice(device.id, device.name, device.type, device.is_active) }
            .toList()
    }

    override fun play(playlistId: String, tracksSections: List<TrackSections>, deviceId: String) {
        val playbackState = spotifyClient.getPlaybackState()
        tracksSections.forEach { trackSections ->
            playNextSongSections(playlistId, trackSections, playbackState.device.volume_percent, deviceId)
        }
    }

    private fun playNextSongSections(playlistId: String, trackSections: TrackSections, baselineVolume: Int, deviceId: String) {
        val sectionsToPlay = findSongSectionsToPlay(trackSections)
        sectionsToPlay.firstOrNull()?.apply {
            val effectiveStartTime = max((this@apply.start * 1000).toInt() - fadeMilliseconds, 0)
            playbackVolumeAdjuster.prepareFadeIn(baselineVolume)
            spotifyClient.playPlaylistTrack(playlistId, trackSections.id, deviceId, effectiveStartTime)
            playbackVolumeAdjuster.fadeIn(baselineVolume)
            runBlocking {
                delay((this@apply.end * 1000 - this@apply.start * 1000).roundToLong())
            }
        }
        sectionsToPlay.drop(1).forEach { sectionToPlay ->
            spotifyClient.seekPlaybackPosition((sectionToPlay.start * 1000).roundToInt())
            runBlocking {
                delay((sectionToPlay.end * 1000 - sectionToPlay.start * 1000).roundToLong())
            }
        }
        playbackVolumeAdjuster.fadeOut(baselineVolume)
    }

    private fun findSongSectionsToPlay(trackSections: TrackSections): List<Section> {
        val distinctOrderedSections = trackSections.sections
            .sortedBy { section -> section.start }
            .map { section -> Section(
                section.start, section.end
            ) }
        return mergeAdjacentSections(distinctOrderedSections)
    }

    private fun mergeAdjacentSections(sections: List<Section>): List<Section> {
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

data class Section(
    var start: Double,
    var end: Double
)