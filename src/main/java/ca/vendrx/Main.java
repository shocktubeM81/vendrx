package ca.vendrx;

import ca.vendrx.audio.AudioInputMonitor;
import ca.vendrx.audio.PreBuffer;
import ca.vendrx.audio.TransmissionDetector;
import ca.vendrx.audio.TransmissionRecorder;
import ca.vendrx.database.TransmissionRepository;
import ca.vendrx.model.Transmission;
import ca.vendrx.audio.AudioDeviceService;

import javax.sound.sampled.*;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("VendRx");
        System.out.println("Radio signal logging and transcription");
        System.out.println();

        TransmissionRepository repository =
                new TransmissionRepository();

        repository.initialize();

        List<Transmission> recentTransmissions =
                repository.findRecent(10);

        System.out.println();

        if (recentTransmissions.isEmpty()) {

            System.out.println(
                    "No previous transmissions."
            );

        } else {

            System.out.println(
                    "Recent transmissions:"
            );

            System.out.println();

            for (int i = 0;
                i < recentTransmissions.size();
                i++) {

                Transmission transmission =
                        recentTransmissions.get(i);

                System.out.printf(
                        "[%d] %s | %.3f s | RMS %.4f%n",
                        i + 1,
                        transmission.getStartTime(),
                        transmission
                                .getDuration()
                                .toMillis() / 1000.0,
                        transmission.getAverageRms()
                );
            }
        }

        System.out.println();

        AudioDeviceService audioDeviceService = new AudioDeviceService();

        List<Mixer.Info> inputs = audioDeviceService.getInputDevices();

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
                            preBuffer,
                            repository
                    );

            monitor.start();

        } catch (LineUnavailableException e) {

            System.out.println(
                    "Unable to open audio input: "
                    + e.getMessage()
            );
        }
    }
}