package dgounaris.dionysus.dionysense

import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSection

class TrackSectionSelectorImpl(private val trackDetailsProvider: TrackDetailsProvider) : TrackSectionSelector {
    private val perSideTotalDuration = 60

    override suspend fun selectSections(trackId: String) : List<TrackSection> {
        val sections = trackDetailsProvider.getTrackDetails(trackId).sections
        val selectedSections = mutableListOf<TrackSection>()

        val pivotSection = (sections
            .mapIndexed { index, trackSection -> Pair(index, trackSection) }
            .sortedByDescending { it.second.loudness }
            .firstOrNull { it.second.confidence > 0.5 } ?: return emptyList())
            .also { selectedSections.add(it.second) }

        val duration = selectedSections.sumOf { it.duration }

        selectedSections.addAll(
                selectPreviousSections(sections, pivotSection.first - 1, duration) +
                selectSubsequentSections(sections, pivotSection.first + 1, duration))

        println("Dionysense TrackSelection: TrackId: $trackId, Sections: ${selectedSections.joinToString(" ") { "[${it.start}-${it.end}]" }}")

        return selectedSections
    }

    private fun selectPreviousSections(sections: List<TrackSection>, evaluatedIndex: Int, duration: Double): List<TrackSection> {
        if (evaluatedIndex < 0 || evaluatedIndex > sections.size - 1) {
            return emptyList()
        }
        if (duration < perSideTotalDuration) {
            listOf(sections[evaluatedIndex]) +
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