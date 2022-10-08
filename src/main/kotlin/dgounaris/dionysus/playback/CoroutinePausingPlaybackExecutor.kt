package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playback.models.PlaybackEvent
import dgounaris.dionysus.playback.models.PlaybackEventType
import dgounaris.dionysus.tracks.models.TrackSections
import kotlinx.coroutines.*
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class CoroutinePausingPlaybackExecutor(
    private val playbackPlanMediator: PlaybackPlanMediator,
    private val playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy,
    private val spotifyClient: SpotifyClient
) : PlaybackExecutor {
    /* todo all the event handlings should use a lock to avoid multithreading interference between pause/queue operations etc */

    override suspend fun handleEvent(playbackEvent: PlaybackEvent) {
        when (playbackEvent.eventType) {
            PlaybackEventType.START ->  { play(playbackEvent.user) }
            PlaybackEventType.STOP -> { stop(playbackEvent.user) }
            PlaybackEventType.PAUSE -> { pause(playbackEvent.user) }
            PlaybackEventType.RESUME -> { play(playbackEvent.user) }
            PlaybackEventType.NEXT -> { next(playbackEvent.user) }
        }
    }

    private fun stop(userId: String) {
        playbackPlanMediator.getActivePlaybackJob(userId)?.cancel("Stop event occurred")
        playbackPlanMediator.clearPlaybackPlanQueue(userId)
        spotifyClient.pausePlayback(userId)
    }

    private suspend fun next(userId: String) {
        playbackPlanMediator.getActivePlaybackJob(userId)?.cancel("Next event occurred")
        play(userId)
    }

    private fun pause(userId: String) {
        playbackPlanMediator.getActivePlaybackJob(userId)?.cancel("Pause event occurred")
        spotifyClient.pausePlayback(userId)
    }

    private suspend fun play(userId: String) {
        coroutineScope {
            launch {
                while (true) {
                    playbackPlanMediator.getNextPlanItem(userId)?.let {
                        playSongSections(userId, it.trackSections, it.playbackDetails)
                    } ?: break
                }
            }.also { playbackPlanMediator.saveActivePlaybackJob(userId, it) }
        }
    }

    private suspend fun playSongSections(
        userId: String,
        trackSections: TrackSections,
        playbackDetails: PlaybackDetails
    ) {
        val playbackVolumeAdjuster = playbackVolumeAdjusterStrategy.getVolumeAdjuster(playbackDetails)
        val fadeMilliseconds = playbackVolumeAdjuster.getFadeMilliseconds()
        trackSections.sections.firstOrNull()?.apply {
            val effectiveStartTime = max((this@apply.start * 1000).toInt() - fadeMilliseconds, 0)
            playbackVolumeAdjuster.prepareFadeIn(userId, playbackDetails.selectedDeviceVolumePercent)
            spotifyClient.playTrack(userId, trackSections.id, playbackDetails.selectedDeviceId, effectiveStartTime)
            playbackVolumeAdjuster.fadeIn(userId, playbackDetails.selectedDeviceVolumePercent)
            if (trackSections.sections.size == 1) {
                delay((this@apply.end * 1000 - this@apply.start * 1000 - fadeMilliseconds).roundToLong())
            } else {
                delay((this@apply.end * 1000 - this@apply.start * 1000).roundToLong())
            }
        }
        trackSections.sections.drop(1).forEach { sectionToPlay ->
            spotifyClient.seekPlaybackPosition(userId, (sectionToPlay.start * 1000).roundToInt())
            // this needs to be "- fadeMilliseconds" only for the last element
            delay((sectionToPlay.end * 1000 - sectionToPlay.start * 1000 - fadeMilliseconds).roundToLong())
        }
        playbackVolumeAdjuster.fadeOut(userId, playbackDetails.selectedDeviceVolumePercent)
    }
}