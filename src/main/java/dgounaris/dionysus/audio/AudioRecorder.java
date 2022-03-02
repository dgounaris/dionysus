package dgounaris.dionysus.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AudioRecorder {

    public AudioRecorder() {

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        Mixer mixer;
        TargetDataLine targetDataLine;

        try{
            AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, false);

            DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

            mixer = AudioSystem.getMixer(mixerInfo[8]); //8, 9 mic
            System.out.println(mixer);
            targetDataLine = (TargetDataLine) mixer.getLine(dataInfo);


            //targetDataLine = (TargetDataLine) AudioSystem.getLine(dataInfo);
            captureThread(targetDataLine, "testwav");

            /*
            if (!AudioSystem.isLineSupported(dataInfo)){
                System.out.println("not sup");
            }



            TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataInfo);

            captureThread(targetDataLine, "testwav");

            */

        }catch (Exception e){
            int a = 1;
        }
    }

    public static void captureThread (TargetDataLine targetDataLine, String filename) throws LineUnavailableException, InterruptedException {
        targetDataLine.open();

        targetDataLine.start();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.invokeAll(
                List.of(
                        new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                AudioInputStream inputStream = new AudioInputStream(targetDataLine);
                                File outputFile = new File("./" + filename + ".wav");
                                try {
                                    AudioSystem.write(inputStream, AudioFileFormat.Type.WAVE, outputFile);
                                } catch (IOException ex){

                                }
                                return null;
                            }
                        }
                ),
                4000,
                TimeUnit.MILLISECONDS
        );

        targetDataLine.stop();
        targetDataLine.close();
    }

}
