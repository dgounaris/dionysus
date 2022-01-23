package audio

import dgounaris.dionysus.audio.AudioLineRecorder
import org.junit.jupiter.api.Test

class AudioLineRecorderTests {
    @Test
    fun aTest() {
        val rec = AudioLineRecorder()
        rec.record(10000)
    }
}