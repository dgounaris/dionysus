package dgounaris.dionysus.playback

class NoopPlaybackVolumeAdjuster : PlaybackVolumeAdjuster {
    override fun getFadeMilliseconds(): Int = 0

    override fun fadeOut(baselineVolume: Int) {
    }

    override fun prepareFadeIn(baselineVolume: Int) {
    }

    override fun fadeIn(baselineVolume: Int) {
    }
}