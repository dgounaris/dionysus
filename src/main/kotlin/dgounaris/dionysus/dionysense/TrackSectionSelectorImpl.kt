package dgounaris.dionysus.dionysense

import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSection

class TrackSectionSelectorImpl(private val trackDetailsProvider: TrackDetailsProvider) : TrackSectionSelector {
    private val minTotalDuration = 40
    private val maxTotalDuration = 100

    override suspend fun selectSections(trackId: String) : List<TrackSection> {
        val sections = trackDetailsProvider.getTrackAnalysis(trackId)
        val selectedSections = mutableListOf<TrackSection>()

        val pivotSection = (sections
            .mapIndexed { index, trackSection -> Pair(index, trackSection) }
            .sortedByDescending { it.second.loudness }
            .firstOrNull { it.second.confidence > 0.5 } ?: return emptyList())
            .also { selectedSections.add(it.second) }

        selectedSections.addAll(getSectionsForMinimumDuration(sections, pivotSection.first-1, pivotSection.second.duration))
        val duration = selectedSections.sumOf { it.duration }

        selectedSections.addAll(
                selectPreviousSections(sections, pivotSection.first - 1, duration) +
                selectSubsequentSections(sections, pivotSection.first + 1, duration))

        println("Dionysense TrackSelection: TrackId: $trackId, Sections: ${selectedSections.joinToString(" ") { "[${it.start}-${it.end}]" }}")

        return selectedSections
    }

    private fun getSectionsForMinimumDuration(sections: List<TrackSection>, indexToOptionallyAdd: Int, totalDuration: Double) : List<TrackSection> {
        if (indexToOptionallyAdd < 0 || indexToOptionallyAdd > sections.size - 1) {
            return emptyList()
        }

        return if (totalDuration <= 40 ) {
            getSectionsForMinimumDuration(
                sections,
                indexToOptionallyAdd - 1,
                totalDuration + sections[indexToOptionallyAdd].duration
            ) + sections[indexToOptionallyAdd]
        } else {
            emptyList()
        }
    }

    private fun selectPreviousSections(sections: List<TrackSection>, evaluatedIndex: Int, duration: Double): List<TrackSection> {
        if (evaluatedIndex < 0 || evaluatedIndex > sections.size - 1 || sections[evaluatedIndex].confidence > 0.7 || duration >= maxTotalDuration) {
            return emptyList()
        }
        return listOf(sections[evaluatedIndex]) +
                selectPreviousSections(sections, evaluatedIndex - 1, duration + sections[evaluatedIndex].duration)
    }

    private fun selectSubsequentSections(sections: List<TrackSection>, evaluatedIndex: Int, duration: Double): List<TrackSection> {
        if (evaluatedIndex < 0 || evaluatedIndex > sections.size - 1 || sections[evaluatedIndex].confidence > 0.7 || duration >= maxTotalDuration) {
            return emptyList()
        }
        return listOf(sections[evaluatedIndex]) +
                selectSubsequentSections(sections, evaluatedIndex + 1, duration + sections[evaluatedIndex].duration)
    }
}