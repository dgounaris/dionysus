package dgounaris.dionysus.storage.tracks

import dgounaris.dionysus.tracks.models.Track
import dgounaris.dionysus.tracks.models.TrackDetails

class MemoryTracksStorage : TracksStorage {
    private val trackStorage: HashMap<String, Track> = hashMapOf()
    private val trackDetailsStorage: HashMap<String, TrackDetails> = hashMapOf()

    override fun save(track: Track) {
        trackStorage[track.id] = track
    }

    override fun save(track: TrackDetails) {
        trackDetailsStorage[track.id] = track
    }

    override fun getTrack(trackId: String) =
        trackStorage[trackId]

    override fun getTrackDetails(trackId: String): TrackDetails? =
        trackDetailsStorage[trackId]
}