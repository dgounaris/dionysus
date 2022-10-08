package dgounaris.dionysus.dionysense

import dgounaris.dionysus.tracks.TrackDetailsProvider

class TrackOrderSelectorImpl(private val trackDetailsProvider: TrackDetailsProvider) : TrackOrderSelector {
    override suspend fun selectOrder(userId: String, trackIds: List<String>) : List<String> {
        val sortedTrackDetails = trackIds.map { trackId -> trackDetailsProvider.getTrackDetails(userId, trackId) }
            .sortedBy { details -> details.features.tempo + details.features.danceability }
        val medianValence = sortedTrackDetails.map { it.features.valence }
            .sorted()[trackIds.size/2]
        val happySongs = sortedTrackDetails.filter { it.features.valence >= medianValence }
        val sadSongs = sortedTrackDetails.filter { it.features.valence < medianValence }

        val splittedTrackDetails = sortedTrackDetails
            .mapIndexed { index, trackDetails -> Pair(index, trackDetails) }
            .groupBy { it.first % 2 }
            .values.toList()

        //return sortedTrackDetails.reversed().map { it.id }

        val bellSortedTrackDetails = happySongs + sadSongs.reversed()
        return bellSortedTrackDetails.map { it.id }
    }
}