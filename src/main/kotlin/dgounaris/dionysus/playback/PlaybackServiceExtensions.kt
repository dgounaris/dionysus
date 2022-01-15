package dgounaris.dionysus.playback

import org.koin.dsl.module

val playbackModule = module {
    single<PlaybackHandler> { PlaybackHandlerImpl(get()) }
}