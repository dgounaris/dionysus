package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.tracks.models.TrackSections
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class PlaybackHandlerImpl(
    private val spotifyClient: SpotifyClient
    ) : PlaybackHandler {

    override fun play(playlistId: String, tracksSections: List<TrackSections>) {
        tracksSections.forEach { trackSections ->
            playNextSongSections(playlistId, trackSections)
        }
    }

    private fun playNextSongSections(playlistId: String, trackSections: TrackSections) {
        val sectionsToPlay = findSongSectionsToPlay(trackSections)
        sectionsToPlay.firstOrNull()?.apply {
            spotifyClient.playPlaylistTrack(playlistId, trackSections.id, (this@apply.start * 1000).toInt())
            runBlocking { delay((this@apply.end * 1000 - this@apply.start * 1000).roundToLong()) }
        }
        sectionsToPlay.drop(1).forEach { sectionToPlay ->
            spotifyClient.seekPlaybackPosition((sectionToPlay.start * 1000).roundToInt())
            runBlocking { delay((sectionToPlay.end * 1000 - sectionToPlay.start * 1000).roundToLong()) }
        }
    }

    private fun findSongSectionsToPlay(trackSections: TrackSections): List<Section> {
        val distinctOrderedSections = trackSections.sections
            .distinctBy { section -> section.start }
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
            if (previousSection.end < sections[index].start - 0.1) { // -0.1 to tolerate arithmetic inaccuracies
                finalSectionList.add(sections[index])
            } else {
                finalSectionList.last().end = max(sections[index].end, finalSectionList.last().end)
            }
            previousSection = sections[index]
        }
        return finalSectionList
    }
}

data class Section(
    val start: Double,
    var end: Double
)