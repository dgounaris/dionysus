package dgounaris.dionysus.playback

import dgounaris.dionysus.clients.SpotifyClient
import dgounaris.dionysus.playback.models.PlaybackDetails
import dgounaris.dionysus.playback.models.PlaybackEvent
import dgounaris.dionysus.playback.models.PlaybackEventType
import dgounaris.dionysus.tracks.models.TrackSections
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.properties.Delegates

class CoroutinePausingPlaybackExecutor(
    private val playbackPlanMediator: PlaybackPlanMediator,
    private val playbackVolumeAdjusterStrategy: PlaybackVolumeAdjusterStrategy,
    private val spotifyClient: SpotifyClient
) : PlaybackExecutor {
    override suspend fun handleEvent(playbackEvent: PlaybackEvent) {
        when (playbackEvent.eventType) {
            PlaybackEventType.START ->  { play(playbackEvent.user) }
            PlaybackEventType.STOP -> { /* todo stop playback, reset playback plan queue */ }
            PlaybackEventType.PAUSE -> { spotifyClient.pausePlayback() }
            PlaybackEventType.RESUME -> { play(playbackEvent.user) }
            PlaybackEventType.NEXT -> { /* todo skip track */ }
        }
    }

    private suspend fun play(user: String) {
        /* todo make this interruptable, stop and pause need to stop this */
        coroutineScope {
            async {
                while (true) {
                    playbackPlanMediator.getNextPlanItem(user)?.let {
                        playSongSections(it.trackSections, it.playbackDetails)
                    } ?: break
                }
            }
        }
    }

    private suspend fun playSongSections(
        trackSections: TrackSections,
        playbackDetails: PlaybackDetails
    ) {
        val playbackVolumeAdjuster = playbackVolumeAdjusterStrategy.getVolumeAdjuster(playbackDetails)
        val fadeMilliseconds = playbackVolumeAdjuster.getFadeMilliseconds()
        trackSections.sections.firstOrNull()?.apply {
            val effectiveStartTime = max((this@apply.start * 1000).toInt() - fadeMilliseconds, 0)
            playbackVolumeAdjuster.prepareFadeIn(playbackDetails.selectedDeviceVolumePercent)
            spotifyClient.playTrack(trackSections.id, playbackDetails.selectedDeviceId, effectiveStartTime)
            playbackVolumeAdjuster.fadeIn(playbackDetails.selectedDeviceVolumePercent)
            if (trackSections.sections.size == 1) {
                delay((this@apply.end * 1000 - this@apply.start * 1000 - fadeMilliseconds).roundToLong())
            } else {
                delay((this@apply.end * 1000 - this@apply.start * 1000).roundToLong())
            }
        }
        trackSections.sections.drop(1).forEach { sectionToPlay ->
            spotifyClient.seekPlaybackPosition((sectionToPlay.start * 1000).roundToInt())
            // this needs to be "- fadeMilliseconds" only for the last element
            delay((sectionToPlay.end * 1000 - sectionToPlay.start * 1000 - fadeMilliseconds).roundToLong())
        }
        playbackVolumeAdjuster.fadeOut(playbackDetails.selectedDeviceVolumePercent)
    }
}