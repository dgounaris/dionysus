package dgounaris.dionysus.audio

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sound.sampled.*

class AudioLineRecorder {
    fun record(millisToRecord: Long) {
        val mixers = AudioSystem.getMixerInfo()
        val line = getAudioLine(mixers.first { it.name.startsWith("Headphones") }) // todo parameterize this
        line.use {
            openLine(it)
            recordLine(it, millisToRecord)
        }
    }

    private fun getAudioLine(mixerInfo: Mixer.Info) =
        AudioSystem.getTargetDataLine(null, mixerInfo)

    private fun openLine(line: DataLine) {
        try {
            line.open()
        } catch (e: LineUnavailableException) {
            throw Exception("Could not open audio line for recording and playback")
        }
    }

    private fun recordLine(line: TargetDataLine, millisToRecord: Long) {
        val out = ByteArrayOutputStream()
        val data = ByteArray(line.bufferSize / 5)
        line.start()
        val executor = Executors.newSingleThreadExecutor()
        executor.invokeAll(
            listOf(Callable {
                while (true) {
                    val numBytesRead = line.read(data, 0, data.size)
                    out.write(data, 0, numBytesRead)
                    val audioInputStream = convertToAudioInputStream(out, line.format, numBytesRead.toLong() / line.format.frameSize)
                    saveAsWav(audioInputStream)
                }
            }),
            millisToRecord,
            TimeUnit.MILLISECONDS
        )
        executor.shutdown()
        line.drain()
    }

    private fun convertToAudioInputStream(output: ByteArrayOutputStream, format: AudioFormat, length: Long) : AudioInputStream {
        val inputStream = ByteArrayInputStream(output.toByteArray())
        return AudioInputStream(inputStream, format, length)
    }

    private fun saveAsWav(input: AudioInputStream) {
        AudioSystem.write(input, AudioFileFormat.Type.WAVE, File("./testwav.wav"))
    }
}