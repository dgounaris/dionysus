package dgounaris.dionysus.tracks

import dgounaris.dionysus.tracks.models.Track
import dgounaris.dionysus.tracks.models.TrackDetails

interface TrackDetailsProvider {
    fun getTrackDetails(track: Track) : TrackDetails
}