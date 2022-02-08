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
        val format = getAudioFormat()
        val info = DataLine.Info(TargetDataLine::class.java, format)
        if (!AudioSystem.isLineSupported(info)) {
            println("Line not supported")
            return
        }
        val line = AudioSystem.getLine(info) as TargetDataLine
        val recorded = ByteArray(48000 * 4 * 4) // 4 seconds
        val data = ByteArray(line.bufferSize)
        val out = ByteArrayOutputStream()
        line.use {
            line.open(format)
            line.start()
            //line.write(recorded, 0, 48000 * 2 * 4)
            val executor = Executors.newSingleThreadExecutor()
            executor.invokeAll(
                listOf(Callable {
                    println("Start capturing")
                    while (true) {
                        val numBytesRead = line.read(data, 0, data.size)
                        if (numBytesRead == -1) break
                        out.write(data, 0, numBytesRead)
                    }
                    AudioSystem.write(AudioInputStream(ByteArrayInputStream(out.toByteArray()), format, out.size() / format.frameSize.toLong()), AudioFileFormat.Type.WAVE, File("./abc.wav"))
                }),
                millisToRecord,
                TimeUnit.MILLISECONDS
            )

            //println("Start capturing")
            //AudioSystem.write(AudioInputStream(line), AudioFileFormat.Type.WAVE, File("./abc.wav"))
            AudioSystem.write(AudioInputStream(ByteArrayInputStream(out.toByteArray()), format, out.size() / format.frameSize.toLong()), AudioFileFormat.Type.WAVE, File("./abc.wav"))
        }
    }

    private fun getAudioFormat(): AudioFormat {
        val sampleRate = 48000f
        val sampleSizeInBits = 16
        val channels = 2
        val signed = true
        val bigEndian = false
        return AudioFormat(
            sampleRate, sampleSizeInBits, channels, signed, bigEndian
        )
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