package dgounaris.dionysus.storage.tracks

import dgounaris.dionysus.tracks.models.Track
import dgounaris.dionysus.tracks.models.TrackDetails

interface TracksStorage {
    fun save(track: Track)
    fun save(track: TrackDetails)
    fun getTrack(trackId: String): Track?
    fun getTrackDetails(trackId: String): TrackDetails?
}