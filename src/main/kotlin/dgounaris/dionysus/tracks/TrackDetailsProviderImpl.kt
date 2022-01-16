package dgounaris.dionysus.tracks

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.tracks.models.*

class TrackDetailsProviderImpl(private val spotifyClient: SpotifyClient) : TrackDetailsProvider {

    override suspend fun getTrackAnalysis(trackId: String): TrackSections =
        TrackSections(
            trackId,
            spotifyClient.getTrackAudioAnalysis(trackId).sections.map { TrackSectionStartEnd(it.start, it.start+it.duration) }
        )

    override suspend fun getTrackDetails(track: Track) : TrackDetails {
        val analysis = spotifyClient.getTrackAudioAnalysis(track.id)
        return TrackDetails(
            track.id,
            track.name,
            analysis.sections.map { TrackSection(
                it.start,
                it.start + it.duration,
                it.tempo,
                it.key,
                it.mode,
                it.timeSignature
            ) }
        )
    }
}