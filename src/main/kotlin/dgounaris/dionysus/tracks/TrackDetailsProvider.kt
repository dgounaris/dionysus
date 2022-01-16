package dgounaris.dionysus.tracks

import dgounaris.dionysus.tracks.models.Track
import dgounaris.dionysus.tracks.models.TrackDetails
import dgounaris.dionysus.tracks.models.TrackSections

interface TrackDetailsProvider {
    suspend fun getTrackAnalysis(trackId: String) : TrackSections
    suspend fun getTrackDetails(track: Track) : TrackDetails
}