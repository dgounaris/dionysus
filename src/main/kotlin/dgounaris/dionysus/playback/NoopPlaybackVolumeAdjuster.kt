package dgounaris.dionysus.playback

class NoopPlaybackVolumeAdjuster : PlaybackVolumeAdjuster {
    override fun getFadeMilliseconds(): Int = 0

    override suspend  fun fadeOut(userId: String, baselineVolume: Int) {
    }

    override suspend  fun fadeIn(userId: String, baselineVolume: Int) {
    }
}