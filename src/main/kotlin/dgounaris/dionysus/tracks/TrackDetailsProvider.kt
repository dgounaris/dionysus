package dgounaris.dionysus.tracks

import dgounaris.dionysus.tracks.models.Track
import dgounaris.dionysus.tracks.models.TrackDetails
import dgounaris.dionysus.tracks.models.TrackSection

interface TrackDetailsProvider {
    suspend fun getTrackAnalysis(trackId: String): List<TrackSection>
    suspend fun getTrackDetails(trackId: String) : TrackDetails
    suspend fun getTrackDetails(track: Track) : TrackDetails
}