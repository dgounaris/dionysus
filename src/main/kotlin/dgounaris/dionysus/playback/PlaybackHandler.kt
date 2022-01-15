package dgounaris.dionysus.playback

import dgounaris.dionysus.playlists.models.Playlist

interface PlaybackHandler {
    fun play(playlist: Playlist, segmentMsList: List<List<Int>>)
}