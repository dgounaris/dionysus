package dgounaris.dionysus.playlists.models

import dgounaris.dionysus.tracks.models.Track

data class Playlist(
    val name: String,
    val id: String,
    val tracks: List<Track>
)