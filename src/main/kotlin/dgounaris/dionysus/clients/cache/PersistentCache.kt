package dgounaris.dionysus.clients.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class PersistentCache : Cache {
    private val jacksonSerializer = ObjectMapper().registerModule(KotlinModule())

    override fun <T> get(key: String, clazz: Class<T>): T? {
        val fileName = ".\\dionysus_cache\\${key}.txt"
        if (!File(fileName).exists()) {
            return null
        }
        return FileReader(fileName).use {
            try {
                jacksonSerializer.readValue(it.readText(), clazz)
            } catch (e: Exception) {
                println("Error while trying to read $key from cache, $e")
                null
            }
        }
    }

    override fun <T> store(key: String, item: T) {
        try {
            val directoryName = ".\\dionysus_cache\\"
            val fileName = ".\\dionysus_cache\\${key}.txt"
            val file = File(fileName)
            Files.createDirectories(Paths.get(directoryName))
            file.createNewFile()
            FileWriter(fileName).use {
                // todo also write datetime?
                it.write(
                    jacksonSerializer.writeValueAsString(item)
                )
            }
        } catch (e: Exception) {
            println("Error while trying to store $key:$item in cache, $e")
        }
    }
}