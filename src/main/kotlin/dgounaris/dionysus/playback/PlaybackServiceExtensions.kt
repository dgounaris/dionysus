package dgounaris.dionysus.playback

import org.koin.dsl.module

val playbackModule = module {
    single<PlaybackVolumeAdjuster> { PlaybackVolumeAdjusterImpl(get()) }
    single<PlaybackHandler> { PlaybackHandlerImpl(get(), get()) }
}