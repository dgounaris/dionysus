package dgounaris.dionysus.tracks

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.tracks.models.*

class TrackDetailsProviderImpl(private val spotifyClient: SpotifyClient) : TrackDetailsProvider {

    override suspend fun getTrackAnalysis(trackId: String) : List<TrackSection> {
        val analysis = spotifyClient.getTrackAudioAnalysis(trackId)
        return analysis.sections.map { TrackSection(
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

    override suspend fun getTrackDetails(trackId: String) : TrackDetails {
        val track = spotifyClient.getTrack(trackId)
        return TrackDetails(
            track.id,
            track.name,
            getTrackAnalysis(track.id)
        ).also {
            println("Track ${track.name} id: ${track.id}")
        }
    }

    override suspend fun getTrackDetails(track: Track) : TrackDetails {
        return TrackDetails(
            track.id,
            track.name,
            getTrackAnalysis(track.id)
        ).also {
            println("Track ${track.name} id: ${track.id}")
        }
    }
}