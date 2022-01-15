package dgounaris.dionysus.common

import java.util.*

class PropertiesLoader {
    companion object {
        fun loadProperties(resourceFileName: String) : Properties {
            return PropertiesLoader::class.java
                .classLoader
                .getResourceAsStream(resourceFileName)
                .use { inputStream ->
                    Properties().also { properties -> properties.load(inputStream) }
                }
        }
    }
}