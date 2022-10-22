package dgounaris.dionysus.playback

import dgounaris.dionysus.playback.models.FadeDetails

class NoopPlaybackVolumeAdjuster : PlaybackVolumeAdjuster {
    override suspend  fun fadeOut(userId: String, baselineVolume: Int, fadeDetails: FadeDetails) {
    }

    override suspend  fun fadeIn(userId: String, baselineVolume: Int, fadeDetails: FadeDetails) {
    }
}