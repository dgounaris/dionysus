package dgounaris.dionysus.storage.user

import dgounaris.dionysus.user.models.User

interface UserStorage {
    fun save(userRecord: User)
    fun getBySpotifyUserId(userId: String): User?
}