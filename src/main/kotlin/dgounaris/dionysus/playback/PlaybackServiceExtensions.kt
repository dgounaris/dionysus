package dgounaris.dionysus.playback

import org.koin.dsl.module

val playbackModule = module {
    single<PlaybackVolumeAdjuster> { LinearPlaybackVolumeAdjuster(get()) }
    single<PlaybackHandler> { PlaybackHandlerImpl(get(), get()) }
}