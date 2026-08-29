package ca.vendrx;

import ca.vendrx.audio.AudioInputMonitor;
import ca.vendrx.audio.PreBuffer;
import ca.vendrx.audio.TransmissionDetector;
import ca.vendrx.audio.TransmissionRecorder;
import ca.vendrx.audio.PreBuffer;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("VendRx");
        System.out.println("Radio signal logging and transcription");
        System.out.println();

        List<Mixer.Info> inputs = getAudioInputs();

        if (inputs.isEmpty()) {
            System.out.println("No audio input device found.");
            return;
        }

        System.out.println("Audio inputs:");
        System.out.println();

        for (int i = 0; i < inputs.size(); i++) {
            System.out.println(
                    "[" + i + "] " + inputs.get(i).getName()
            );
        }

        System.out.println();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Select input: ");

        int selection = scanner.nextInt();

        if (selection < 0 || selection >= inputs.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        try {

            AudioFormat format = new AudioFormat(
                    44100.0f,
                    16,
                    1,
                    true,
                    false
            );

            TransmissionDetector detector =
                    new TransmissionDetector(
                            0.20,   // seuil RMS
                            3000    // 3 secondes de silence
                    );

            TransmissionRecorder recorder =
                    new TransmissionRecorder(format);

            int bytesPerSecond =
                    (int) (
                            format.getSampleRate()
                            * format.getFrameSize()
                    );

            PreBuffer preBuffer =
                    new PreBuffer(
                            bytesPerSecond * 2
                    );

            AudioInputMonitor monitor =
                    new AudioInputMonitor(
                            inputs.get(selection),
                            detector,
                            recorder,
                            preBuffer
                    );

            monitor.start();

        } catch (LineUnavailableException e) {

            System.out.println(
                    "Unable to open audio input: "
                    + e.getMessage()
            );
        }
    }

    private static List<Mixer.Info> getAudioInputs() {

        List<Mixer.Info> inputs = new ArrayList<>();

        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {

            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            for (Line.Info lineInfo : mixer.getTargetLineInfo()) {

                if (TargetDataLine.class.isAssignableFrom(
                        lineInfo.getLineClass()
                )) {

                    inputs.add(mixerInfo);
                    break;
                }
            }
        }

        return inputs;
    }
}