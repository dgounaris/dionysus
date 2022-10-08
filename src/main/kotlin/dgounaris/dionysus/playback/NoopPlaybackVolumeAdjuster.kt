package dgounaris.dionysus.playback

class NoopPlaybackVolumeAdjuster : PlaybackVolumeAdjuster {
    override fun getFadeMilliseconds(): Int = 0

    override fun fadeOut(userId: String, baselineVolume: Int) {
    }

    override fun prepareFadeIn(userId: String, baselineVolume: Int) {
    }

    override fun fadeIn(userId: String, baselineVolume: Int) {
    }
}