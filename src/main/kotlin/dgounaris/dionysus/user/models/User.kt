package dgounaris.dionysus.user.models

data class User(
    val spotifyUserId: String,
    val spotifyAccessToken: String?,
    val spotifyRefreshToken : String?,
    val isLoggedIn: Boolean = false
)