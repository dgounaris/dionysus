package dgounaris.dionysus.dionysense

interface TrackOrderSelector {
    suspend fun selectOrder(userId: String, trackIds: List<String>) : List<String>
}