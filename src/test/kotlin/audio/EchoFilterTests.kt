package audio

import dgounaris.dionysus.audio.EchoFilter
import dgounaris.dionysus.audio.FilteredSoundStream
import dgounaris.dionysus.audio.SimpleSoundPlayer
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class EchoFilterTests {
    @Test
    fun aTest() {
        val sound = SimpleSoundPlayer("C:\\Users\\dimit\\Documents\\repos\\dionysus\\src\\test\\kotlin\\audio\\file_example_WAV_1MG.wav")
        val inputStream = ByteArrayInputStream(sound.samples)
        val filter = EchoFilter(4000, .8f)
        val filteredInputStream = FilteredSoundStream(inputStream, filter)
        sound.play(filteredInputStream)
    }
}