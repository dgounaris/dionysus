package dgounaris.dionysus.dionysense

import dgounaris.dionysus.tracks.models.TrackSection

interface TrackSectionSelector {
    suspend fun selectSections(userId: String, trackId: String) : List<TrackSection>
}