package dgounaris.dionysus.dionysense

import dgounaris.dionysus.dionysense.models.OrderSelectionStrategy
import dgounaris.dionysus.tracks.TrackDetailsProvider
import dgounaris.dionysus.tracks.models.Track

class TrackOrderSelectorImpl(private val trackDetailsProvider: TrackDetailsProvider) : TrackOrderSelector {
    override suspend fun selectOrder(userId: String, trackIds: List<String>, orderSelectionStrategy: OrderSelectionStrategy) : List<Track> {
        return when (orderSelectionStrategy) {
            OrderSelectionStrategy.NONE -> selectOrderByNone(userId, trackIds)
            OrderSelectionStrategy.RANDOM -> selectOrderByRandom(userId, trackIds)
            OrderSelectionStrategy.DEFAULT -> selectOrderByDefault(userId, trackIds)
        }
    }

    private suspend fun selectOrderByNone(userId: String, trackIds: List<String>) : List<Track> {
        val trackDetails = trackIds.map { trackId -> trackDetailsProvider.getTrackDetails(userId, trackId) }
        return trackDetails.map { Track(it.name, it.id) }
    }

    private suspend fun selectOrderByRandom(userId: String, trackIds: List<String>) : List<Track> {
        val trackDetails = trackIds.map { trackId -> trackDetailsProvider.getTrackDetails(userId, trackId) }
        return trackDetails.map{ Track(it.name, it.id) }.shuffled()
    }

    private suspend fun selectOrderByDefault(userId: String, trackIds: List<String>) : List<Track> {
        val trackDetails = trackIds.map { trackId -> trackDetailsProvider.getTrackDetails(userId, trackId) }
        val medianValence = trackDetails.map { it.features?.valence ?: 0.5 }
            .sorted()[trackIds.size/2]
        /*val happySongs = trackDetails.filter { (it.features?.valence ?: 0.5) >= medianValence }
            .sortedBy { details ->
                (details.features?.danceability ?: 0.5) +
                (details.features?.energy ?: 0.5) +
                (details.features?.valence ?: 0.5)
            }
        val sadSongs = trackDetails.filter { (it.features?.valence ?: 0.5) < medianValence }
            .sortedByDescending { details ->
                (details.features?.danceability ?: 0.5) +
                (details.features?.energy ?: 0.5) +
                (details.features?.valence ?: 0.5)
            }*/
        val happySongs = trackDetails.filter { (it.features?.valence ?: 0.5) >= medianValence }
            .sortedBy { details ->
                (details.features?.tempo ?: 0.5)
            }
        val sadSongs = trackDetails.filter { (it.features?.valence ?: 0.5) < medianValence }
            .sortedByDescending { details ->
                (details.features?.tempo ?: 0.5)
            }

        val bellSortedTrackDetails = happySongs + sadSongs
        return bellSortedTrackDetails.map { Track(it.name, it.id) }
    }
}