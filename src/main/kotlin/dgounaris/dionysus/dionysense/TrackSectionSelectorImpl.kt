package dgounaris.dionysus.dionysense

import dgounaris.dionysus.dionysense.models.SelectionOptions
import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSection

class TrackSectionSelectorImpl(private val trackDetailsProvider: TrackDetailsProvider) : TrackSectionSelector {
    override suspend fun selectSections(userId: String, trackId: String, selectionOptions: SelectionOptions) : List<TrackSection> {
        val sections = trackDetailsProvider.getTrackDetails(userId, trackId).sections

        val selectedSections = selectSectionsByBestSectionGroup(
            sections, selectionOptions.minimumSelectionDuration, selectionOptions.maximumSelectionDuration)

        return selectedSections.also {
            println("Dionysense TrackSelection: TrackId: $trackId, Sections: ${it.joinToString(" ") { "[${it.start}-${it.end}]" }}")
        }
    }

    private fun selectSectionsByBestSectionGroup(sections: List<TrackSection>, minimumDuration: Int, maximumDuration: Int) : List<TrackSection> {
        val sortedSections = sections.sortedBy { it.start }
        val sectionGroups = filterOutSectionsThatCanNotGenerateGroup(sortedSections).mapIndexed { i, it ->
            listOf(it) + createGroupFromSections(sortedSections, i, minimumDuration, maximumDuration)
        }
        return sectionGroups.maxByOrNull { group -> group.sumOf { section -> section.loudness }/group.size }!!
    }

    private fun filterOutSectionsThatCanNotGenerateGroup(sections: List<TrackSection>): List<TrackSection> {
        var duration = 0.0
        var lookbackIndex = sections.lastIndex - 1
        return sections.dropLastWhile { section ->
            (lookbackIndex != -1 && duration + section.duration + sections[lookbackIndex].duration < 100).also {
            duration += section.duration
            lookbackIndex -= 1
        } }
    }

    private fun createGroupFromSections(sections: List<TrackSection>, currentIndex: Int, minimumDuration: Int, maximumDuration: Int): List<TrackSection> {
        var duration = sections[currentIndex].duration
        return sections.drop(currentIndex+1).takeWhile { section ->
            (duration + section.duration <= maximumDuration || duration < minimumDuration)
                .also { duration += section.duration }
        }
    }
}