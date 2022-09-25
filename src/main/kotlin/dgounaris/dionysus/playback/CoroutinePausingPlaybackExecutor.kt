package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.tracks.models.TrackSections
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class CoroutinePausingPlaybackExecutor(
    private val spotifyClient: SpotifyClient
) : PlaybackExecutor {
    override fun playSongSections(
        trackSections: TrackSections,
        playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy,
        playbackDetails: PlaybackDetails
    ) {
        val playbackVolumeAdjuster = playbackVolumeAdjusterStrategy.getVolumeAdjuster(playbackDetails)
        val fadeMilliseconds = playbackVolumeAdjuster.getFadeMilliseconds()
        trackSections.sections.firstOrNull()?.apply {
            val effectiveStartTime = max((this@apply.start * 1000).toInt() - fadeMilliseconds, 0)
            playbackVolumeAdjuster.prepareFadeIn(playbackDetails.selectedDeviceVolumePercent)
            spotifyClient.playTrack(trackSections.id, playbackDetails.selectedDeviceId, effectiveStartTime)
            playbackVolumeAdjuster.fadeIn(playbackDetails.selectedDeviceVolumePercent)
            runBlocking {
                if (trackSections.sections.size == 1) {
                    delay((this@apply.end * 1000 - this@apply.start * 1000 - fadeMilliseconds).roundToLong())
                } else {
                    delay((this@apply.end * 1000 - this@apply.start * 1000).roundToLong())
                }
            }
        }
        trackSections.sections.drop(1).forEach { sectionToPlay ->
            spotifyClient.seekPlaybackPosition((sectionToPlay.start * 1000).roundToInt())
            runBlocking {
                delay((sectionToPlay.end * 1000 - sectionToPlay.start * 1000 - fadeMilliseconds).roundToLong())
            }
        }
        playbackVolumeAdjuster.fadeOut(playbackDetails.selectedDeviceVolumePercent)
    }

    override suspend fun play(user: String, playbackPlanMediator: PlaybackPlanMediator, playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy) {
        coroutineScope {
            async {
                playbackPlanMediator.getNextPlanItem(user)?.let {
                    playSongSections(it.trackSections, playbackVolumeAdjusterStrategy, it.playbackDetails)
                }
            }
        }
    }
}