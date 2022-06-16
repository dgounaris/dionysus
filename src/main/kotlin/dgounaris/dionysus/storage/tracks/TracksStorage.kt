package dgounaris.dionysus.storage.tracks

import dgounaris.dionysus.tracks.models.Track

interface TracksStorage {
    fun save(track: Track)
    fun getTrack(trackId: String): Track?
}