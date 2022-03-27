package dgounaris.dionysus.storage.user

import dgounaris.dionysus.user.models.User

class MemoryUserStorage : UserStorage {
    private val storage: MutableList<User> = mutableListOf()

    override fun save(userRecord: User) {
        storage.add(userRecord)
    }

    override fun getBySpotifyUserId(userId: String): User? =
        storage.firstOrNull { it.spotifyUserId == userId }
}