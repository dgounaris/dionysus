package dgounaris.dionysus.view

import kotlinx.html.*

fun currentUserPlaylistsSelectionView(html: HTML, currentUserPlaylists: List<String>) {
    html.body {
        form {
            action = "http://localhost:8888/playlists/tracks"
            method = FormMethod.get
            label {
                htmlFor = "playlists"
                +"Choose a playlist:"
            }
            select {
                id = "playlistName"
                name = "playlistName"
                currentUserPlaylists.map { playlistName ->
                    option {
                        value = playlistName
                        +playlistName
                    }
                }
            }
            input {
                type = InputType.submit
                value = "Submit"
            }
        }
    }
}