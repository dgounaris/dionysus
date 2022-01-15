package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.clients.models.TrackAudioAnalysisResponseDto
import dgounaris.dionysus.playlists.models.Playlist
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class PlaybackHandlerImpl(private val spotifyClient: SpotifyClient) : PlaybackHandler {
    override fun play(playlist: Playlist, segmentMsList: List<List<Int>>) {
        val trackAnalyses = playlist.tracks.map { track -> spotifyClient.getTrackAudioAnalysis(track.id) }
        println("Starting automated playback in 10 seconds, prepare your system...")
        runBlocking { delay(10000) }
        trackAnalyses.zip(segmentMsList).forEachIndexed { index, trackSegmentPair ->
            playNextSongSections(trackSegmentPair.first, trackSegmentPair.second, index)
        }
    }

    private fun playNextSongSections(analysis: TrackAudioAnalysisResponseDto, sectionsMs: List<Int>, index: Int) {
        val sectionsToPlay = findSongSectionsToPlay(analysis, sectionsMs)
        if (index != 0) {
            // don't skip first song, can be done in a better designed way later
            spotifyClient.playNext()
        }
        sectionsToPlay.forEach { sectionToPlay ->
            spotifyClient.seekPlaybackPosition(sectionToPlay.start)
            runBlocking { delay(sectionToPlay.duration.toLong()) }
        }
    }

    private fun findSongSectionsToPlay(analysis: TrackAudioAnalysisResponseDto, sectionsMs: List<Int>): List<Section> {
        if (sectionsMs.isEmpty()) {
            return emptyList()
        }
        val sectionsToPlay = sectionsMs.map { sectionMs -> findSongSectionToPlay(analysis, sectionMs) }
        val distinctOrderedSections = sectionsToPlay
            .distinctBy { section -> section.start }
            .sortedBy { section -> section.start }
            .map { section -> Section((section.start * 1000).roundToInt(), (section.duration * 1000).roundToInt()) }
            .toMutableList()
        return mergeAdjacentSections(distinctOrderedSections)
    }

    private fun mergeAdjacentSections(sections: List<Section>): List<Section> {
        var previousSection = sections.first()
        val finalSectionList = mutableListOf(previousSection)
        for(index in 1 until sections.size) {
            if (previousSection.start + previousSection.duration != sections[index].start) {
                finalSectionList.add(sections[index])
            } else {
                finalSectionList.last().duration += sections[index].duration
            }
            previousSection = sections[index]
        }
        return finalSectionList
    }

    private fun findSongSectionToPlay(analysis: TrackAudioAnalysisResponseDto, sectionMs: Int) =
        analysis.sections.first {
                section -> (section.start * 1000).roundToInt() <= sectionMs &&
                ((section.start + section.duration) * 1000).roundToInt() >= sectionMs
        }
}

data class Section(
    val start: Int,
    var duration: Int
)