package dgounaris.dionysus.storage.user

import dgounaris.dionysus.user.models.User

class MemoryUserStorage : UserStorage {
    private val storage: HashMap<String, User> = hashMapOf()

    override fun save(userRecord: User) {
        storage[userRecord.spotifyUserId] = userRecord
    }

    override fun getBySpotifyUserId(userId: String): User? =
        storage.getOrDefault(userId, null)
}