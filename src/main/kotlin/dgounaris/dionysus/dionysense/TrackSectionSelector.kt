package dgounaris.dionysus.dionysense

import dgounaris.dionysus.dionysense.models.SectionSelectionOptions
import dgounaris.dionysus.tracks.models.TrackSection

interface TrackSectionSelector {
    suspend fun selectSections(userId: String, trackId: String, selectionOptions: SectionSelectionOptions) : List<TrackSection>
}