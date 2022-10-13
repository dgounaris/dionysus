package dgounaris.dionysus.dionysense

import dgounaris.dionysus.dionysense.models.SelectionOptions
import dgounaris.dionysus.tracks.models.TrackSection

interface TrackSectionSelector {
    suspend fun selectSections(userId: String, trackId: String, selectionOptions: SelectionOptions) : List<TrackSection>
}