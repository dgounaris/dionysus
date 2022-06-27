package dgounaris.dionysus.tracks

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.storage.tracks.TracksStorage
import dgounaris.dionysus.tracks.models.*

class TrackDetailsProviderImpl(
    private val spotifyClient: SpotifyClient,
    private val tracksStorage: TracksStorage
    ) : TrackDetailsProvider {

    override suspend fun getTrackDetails(trackId: String) : TrackDetails {
        val persistedDetails = tracksStorage.getTrackDetails(trackId)
        if (persistedDetails != null) {
            return persistedDetails
        }
        val track = spotifyClient.getTrack(trackId)
        return TrackDetails(
            track.id,
            track.name,
            getTrackAnalysis(track.id) ?: emptyList(),
            getTrackFeatures(track.id)
        ).also { tracksStorage.save(it) }
         .also {
            println("Track ${track.name} id: ${track.id}")
        }
    }

    private suspend fun getTrackAnalysis(trackId: String) : List<TrackSection>? {
        val analysis = spotifyClient.getTrackAudioAnalysis(trackId)
        return analysis?.sections?.map { TrackSection(
            it.start,
            it.duration,
            it.start + it.duration,
            it.confidence,
            it.loudness,
            it.tempo,
            it.key,
            it.mode,
            it.timeSignature
        ) }
    }

    private suspend fun getTrackFeatures(trackId: String) : TrackAudioFeatures {
        val features = spotifyClient.getTrackAudioFeatures(trackId)
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