package dgounaris.dionysus.dionysense

interface TrackOrderSelector {
    suspend fun selectOrder(trackIds: List<String>) : List<String>
}