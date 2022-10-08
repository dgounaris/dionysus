package dgounaris.dionysus.dionysense

import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSection
import dgounaris.dionysus.tracks.models.TrackSections

class TrackSectionSelectorImpl(private val trackDetailsProvider: TrackDetailsProvider) : TrackSectionSelector {
    private val perSideTotalDuration = 40

    override suspend fun selectSections(userId: String, trackId: String) : List<TrackSection> {
        val sections = trackDetailsProvider.getTrackDetails(userId, trackId).sections

        val selSections1 = test1(sections)

        return if (selSections1.contains(sections.last())) {
            selSections1.dropLast(1) + selSections1.last().copy(end = selSections1.last().end - 4)
        } else {
            selSections1
        }.also {
            println("Dionysense TrackSelection: TrackId: $trackId, Sections: ${it.joinToString(" ") { "[${it.start}-${it.end}]" }}")
        }
    }

    private fun test1(sections: List<TrackSection>) : List<TrackSection> {
        val sortedSections = sections.sortedBy { it.start }
        val sectionGroups = test1helpRev(sortedSections).mapIndexed { i, it ->
            listOf(it) + test1help(sortedSections, i)
        }
        return sectionGroups.maxByOrNull { group -> group.sumOf { section -> section.loudness }/group.size }!!
    }

    private fun test1helpRev(sections: List<TrackSection>): List<TrackSection> {
        var duration = 0.0
        var lookbackIndex = sections.lastIndex - 1
        return sections.dropLastWhile { section ->
            (lookbackIndex != -1 && duration + section.duration + sections[lookbackIndex].duration < 100).also {
            duration += section.duration
            lookbackIndex -= 1
        } }
    }

    private fun test1help(sections: List<TrackSection>, currentIndex: Int): List<TrackSection> {
        var duration = sections[currentIndex].duration
        return sections.drop(currentIndex+1).takeWhile { section ->
            (duration + section.duration <= 100 || duration < 75)
                .also { duration += section.duration }
        }
    }

    private fun selectPreviousSections(sections: List<TrackSection>, evaluatedIndex: Int, duration: Double): List<TrackSection> {
        if (evaluatedIndex < 0 || evaluatedIndex > sections.size - 1) {
            return emptyList()
        }
        if (duration < perSideTotalDuration) {
            return listOf(sections[evaluatedIndex]) +
                    selectPreviousSections(sections, evaluatedIndex - 1, duration + sections[evaluatedIndex].duration)
        }
        if (sections[evaluatedIndex].confidence > 0.7 || duration >= perSideTotalDuration) {
            return emptyList()
        }
        return listOf(sections[evaluatedIndex]) +
                selectPreviousSections(sections, evaluatedIndex - 1, duration + sections[evaluatedIndex].duration)
    }

    private fun selectSubsequentSections(sections: List<TrackSection>, evaluatedIndex: Int, duration: Double): List<TrackSection> {
        if (evaluatedIndex < 0 || evaluatedIndex > sections.size - 1) {
            return emptyList()
        }
        if (duration < perSideTotalDuration) {
            return listOf(sections[evaluatedIndex]) +
                    selectSubsequentSections(sections, evaluatedIndex + 1, duration + sections[evaluatedIndex].duration)
        }
        if (sections[evaluatedIndex].confidence > 0.7 || duration >= perSideTotalDuration) {
            return emptyList()
        }
        return listOf(sections[evaluatedIndex]) +
                selectSubsequentSections(sections, evaluatedIndex + 1, duration + sections[evaluatedIndex].duration)
    }
}