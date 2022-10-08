package dgounaris.dionysus.tracks

import dgounaris.dionysus.tracks.models.TrackDetails

interface TrackDetailsProvider {
    suspend fun getTrackDetails(userId: String, trackId: String) : TrackDetails
}