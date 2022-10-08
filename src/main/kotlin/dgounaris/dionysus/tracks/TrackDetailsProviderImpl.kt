package dgounaris.dionysus.tracks

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.storage.tracks.TracksStorage
import dgounaris.dionysus.tracks.models.*

class TrackDetailsProviderImpl(
    private val spotifyClient: SpotifyClient,
    private val tracksStorage: TracksStorage
    ) : TrackDetailsProvider {

    override suspend fun getTrackDetails(userId: String, trackId: String) : TrackDetails {
        val persistedDetails = tracksStorage.getTrackDetails(trackId)
        if (persistedDetails != null) {
            return persistedDetails
        }
        val track = spotifyClient.getTrack(userId, trackId)
        return TrackDetails(
            track.id,
            track.name,
            getTrackAnalysis(userId, track.id) ?: emptyList(),
            getTrackFeatures(userId, track.id)
        ).also { tracksStorage.save(it) }
         .also {
            println("Track ${track.name} id: ${track.id}")
        }
    }

    private suspend fun getTrackAnalysis(userId: String, trackId: String) : List<TrackSection>? {
        val analysis = spotifyClient.getTrackAudioAnalysis(userId, trackId)
        return analysis?.sections?.map { TrackSection(
            it.start,
            it.duration,
            it.start + it.duration,
            it.confidence,
            it.loudness,
            it.tempo,
            it.key,
            it.mode,
            it.time_signature
        ) }
    }

    private suspend fun getTrackFeatures(userId: String, trackId: String) : TrackAudioFeatures {
        val features = spotifyClient.getTrackAudioFeatures(userId, trackId)
        return TrackAudioFeatures(
            features.acousticness,
            features.danceability,
            features.energy,
            features.instrumentalness,
            features.key,
            features.liveness,
            features.loudness,
            features.speechiness,
            features.tempo,
            features.time_signature,
            features.valence
        )
    }
}