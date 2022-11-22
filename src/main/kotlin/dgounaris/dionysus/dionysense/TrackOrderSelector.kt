package dgounaris.dionysus.dionysense

import dgounaris.dionysus.dionysense.models.OrderSelectionStrategy
import dgounaris.dionysus.tracks.models.Track

interface TrackOrderSelector {
    suspend fun selectOrder(userId: String, trackIds: List<String>, orderSelectionStrategy: OrderSelectionStrategy) : List<Track>
}