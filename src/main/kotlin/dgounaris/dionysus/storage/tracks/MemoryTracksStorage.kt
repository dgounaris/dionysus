package dgounaris.dionysus.storage.tracks

import dgounaris.dionysus.tracks.models.Track

class MemoryTracksStorage : TracksStorage {
    private val storage: HashMap<String, Track> = hashMapOf()

    override fun save(track: Track) {
        storage[track.id] = track
    }

    override fun getTrack(trackId: String) =
        storage[trackId]
}