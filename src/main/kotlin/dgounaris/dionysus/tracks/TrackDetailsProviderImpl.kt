package dgounaris.dionysus.tracks

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.tracks.models.Track
import dgounaris.dionysus.tracks.models.TrackDetails
import dgounaris.dionysus.tracks.models.TrackSection

class TrackDetailsProviderImpl(private val spotifyClient: SpotifyClient) : TrackDetailsProvider {
    override fun getTrackDetails(track: Track) : TrackDetails {
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