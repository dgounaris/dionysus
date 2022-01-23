package dgounaris.dionysus.dionysense

import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.TrackSection
import dgounaris.dionysus.tracks.models.TrackSections

class TrackSectionSelectorImpl(private val trackDetailsProvider: TrackDetailsProvider) : TrackSectionSelector {

    override suspend fun selectSections(trackId: String) : List<TrackSection> {
        val sections = trackDetailsProvider.getTrackAnalysis(trackId)
        val selectedSections = mutableListOf<TrackSection>()

        val pivotSection = (sections
            .mapIndexed { index, trackSection -> Pair(index, trackSection) }
            .sortedByDescending { it.second.loudness }
            .firstOrNull { it.second.confidence > 0.5 } ?: return emptyList())
            .also { selectedSections.add(it.second) }

        addSectionsForMinimumDuration(sections, pivotSection.first, pivotSection.second.duration)

        return selectedSections +
                selectPreviousSections(sections, pivotSection.first - 1) +
                selectSubsequentSections(sections, pivotSection.first + 1)
    }

    private fun addSectionsForMinimumDuration(sections: List<TrackSection>, indexToOptionallyAdd: Int, totalDuration: Double) : List<TrackSection> {
        if (indexToOptionallyAdd < 0 || indexToOptionallyAdd > sections.size - 1) {
            return emptyList()
        }

        return if (totalDuration <= 40) {
            addSectionsForMinimumDuration(
                sections,
                indexToOptionallyAdd - 1,
                totalDuration + sections[indexToOptionallyAdd].duration
            ) + sections[indexToOptionallyAdd]
        } else {
            emptyList()
        }
    }

    private fun selectPreviousSections(sections: List<TrackSection>, evaluatedIndex: Int): List<TrackSection> {
        if (evaluatedIndex < 0 || evaluatedIndex > sections.size - 1 || sections[evaluatedIndex].confidence > 0.7) {
            return emptyList()
        }
        return listOf(sections[evaluatedIndex]) +
                selectPreviousSections(sections, evaluatedIndex - 1)
    }

    private fun selectSubsequentSections(sections: List<TrackSection>, evaluatedIndex: Int): List<TrackSection> {
        if (evaluatedIndex < 0 || evaluatedIndex > sections.size - 1 || sections[evaluatedIndex].confidence > 0.7) {
            return emptyList()
        }
        return listOf(sections[evaluatedIndex]) +
                selectSubsequentSections(sections, evaluatedIndex + 1)
    }
}