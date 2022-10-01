package dgounaris.dionysus.playback

import org.koin.dsl.module

val playbackModule = module {
    single<PlaybackVolumeAdjusterStrategy> { PlaybackVolumeAdjusterStrategyImpl(get()) }
    single<PlaybackStatusPollingService> { PlaybackStatusShortPollingService(get()) }
    single<PlaybackVolumeAdjuster> { LinearPlaybackVolumeAdjuster(get()) }
    single<PlaybackOrchestrator> { SectionMergingPlaybackOrchestrator(get(), get(), get(), get()) }
    single<PlaybackExecutor> { CoroutinePausingPlaybackExecutor(get()) }
    single<PlaybackPlanMediator> { SimplePlaybackPlanMediator() }
}