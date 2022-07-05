package dgounaris.dionysus.common

import java.util.*

class PropertiesProvider {
    companion object {
        val configuration : Properties = PropertiesLoader.loadProperties("dev.app.properties")
    }
}