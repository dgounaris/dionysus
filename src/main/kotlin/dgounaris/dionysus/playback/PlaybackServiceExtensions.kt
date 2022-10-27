package dgounaris.dionysus.playback

import org.koin.dsl.module

val playbackModule = module {
    single<PlaybackVolumeAdjusterStrategy> { PlaybackVolumeAdjusterStrategyImpl(get()) }
    single<PlaybackVolumeAdjuster> { LinearPlaybackVolumeAdjuster(get()) }
    single<PlaybackOrchestrator> { SectionMergingPlaybackOrchestrator(get(), get(), get()) }
    single<PlaybackExecutor> { CoroutinePausingPlaybackExecutor(get(), get(), get()) }
    single<PlaybackPlanMediator> { SimplePlaybackPlanMediator() }
    single<PlaybackEventHandler> { SimplePlaybackEventHandler(get()) }
}