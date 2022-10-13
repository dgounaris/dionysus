package dgounaris.dionysus.dionysense

import dgounaris.dionysus.tracks.models.Track

interface TrackOrderSelector {
    suspend fun selectOrder(userId: String, trackIds: List<String>) : List<Track>
}