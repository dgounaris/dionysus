package dgounaris.dionysus.clients.models

data class CurrentUserPlaylistsResponseDto(
    val items: List<PlaylistItem>

)

data class PlaylistItem(
    val name: String,
    val id: String
)