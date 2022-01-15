package dgounaris.dionysus

import dgounaris.dionysus.clients.clientsModule
import dgounaris.dionysus.playback.playbackModule
import dgounaris.dionysus.playlists.playlistsModule
import dgounaris.dionysus.server.Server
import dgounaris.dionysus.server.serverModule
import dgounaris.dionysus.tracks.tracksModule
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin

fun main(vararg args: String) {
    startKoin {
        modules(
            clientsModule,
            tracksModule,
            playlistsModule,
            playbackModule,
            serverModule
        )
    }
    val server = get().get<Server>(Server::class)
    server.start()
}