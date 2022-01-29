package dgounaris.dionysus.clients.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class PersistentCache : Cache {
    private val jacksonSerializer = ObjectMapper().registerModule(KotlinModule())

    override fun <T> get(key: String, clazz: Class<T>): T {
        val fileName = "C:\\Users\\dimit\\Documents\\repos\\dionysus\\dionysus_cache\\${key}.txt"
        return FileReader(fileName).use {
            try {
                jacksonSerializer.readValue(it.readText(), clazz)
            } catch (e: Exception) {
                println("Error while trying to read $key from cache")
                throw Exception("Error while trying to read $key from cache")
            }
        }
    }

    override fun <T> store(key: String, item: T) {
        try {
            val fileName = "C:\\Users\\dimit\\Documents\\repos\\dionysus\\dionysus_cache\\${key}.txt"
            val file = File(fileName)
            file.createNewFile()
            FileWriter(fileName).use {
                // todo also write datetime?
                it.write(
                    jacksonSerializer.writeValueAsString(item)
                )
            }
        } catch (e: Exception) {
            println("Error while trying to store $key:$item in cache")
        }
    }
}