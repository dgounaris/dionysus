package dgounaris.dionysus.clients.cache

interface Cache {
    fun <T> store(key: String, item: T)
    fun <T> get(key: String, clazz: Class<T>): T
}