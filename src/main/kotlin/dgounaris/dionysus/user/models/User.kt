package dgounaris.dionysus.user.models

data class User(
    val spotifyUserId: String,
    val accessToken: String?,
    val refreshToken : String?,
    val isLoggedIn: Boolean = false
)