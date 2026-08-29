package ca.vendrx.audio;

import ca.vendrx.database.TransmissionRepository;
import ca.vendrx.model.Transmission;

import javax.sound.sampled.*;

public class AudioInputMonitor {

    private final Mixer.Info mixerInfo;
    private final AudioFormat format;

    private final TransmissionDetector transmissionDetector;
    private final TransmissionRecorder recorder;
    private final PreBuffer preBuffer;
    private final TransmissionRepository repository;
    private final AudioMonitorListener listener;

    private volatile boolean running = false;
    private volatile TargetDataLine line;

    public AudioInputMonitor(
            Mixer.Info mixerInfo,
            AudioFormat format,
            TransmissionDetector transmissionDetector,
            TransmissionRecorder recorder,
            PreBuffer preBuffer,
            TransmissionRepository repository,
            AudioMonitorListener listener
    ) {
        this.mixerInfo = mixerInfo;
        this.format = format;
        this.transmissionDetector = transmissionDetector;
        this.recorder = recorder;
        this.preBuffer = preBuffer;
        this.repository = repository;
        this.listener = listener;
    }

    public void start() throws LineUnavailableException {

        Mixer mixer =
                AudioSystem.getMixer(mixerInfo);

        DataLine.Info lineInfo =
                new DataLine.Info(
                        TargetDataLine.class,
                        format
                );

        TargetDataLine activeLine =
                (TargetDataLine) mixer.getLine(lineInfo);

        activeLine.open(format);
        activeLine.start();

        line = activeLine;
        running = true;

        System.out.println();
        System.out.println(
                "Monitoring: " + mixerInfo.getName()
        );
        System.out.println(
                "Ctrl+C to stop."
        );
        System.out.println();

        byte[] buffer = new byte[4096];

        try {

            while (running) {

                int bytesRead;

                try {

                    bytesRead =
                            activeLine.read(
                                    buffer,
                                    0,
                                    buffer.length
                            );

                } catch (IllegalStateException e) {

                    if (!running) {
                        break;
                    }

                    throw e;
                }

                if (bytesRead <= 0) {
                    continue;
                }

                double rms =
                        calculateRms(
                                buffer,
                                bytesRead
                        );

                TransmissionDetector.State previousState =
                        transmissionDetector.getState();

                transmissionDetector.update(rms);

                TransmissionDetector.State currentState =
                        transmissionDetector.getState();

                if (
                        previousState == TransmissionDetector.State.IDLE
                        &&
                        currentState == TransmissionDetector.State.RECORDING
                ) {

                    recorder.start();

                    byte[] bufferedAudio =
                            preBuffer.getAudio();

                    recorder.append(
                            bufferedAudio,
                            bufferedAudio.length
                    );
                }

                if (
                        currentState == TransmissionDetector.State.RECORDING
                ) {

                    recorder.append(
                            buffer,
                            bytesRead
                    );

                    recorder.addRms(rms);
                }

                if (
                        previousState == TransmissionDetector.State.RECORDING
                        &&
                        currentState == TransmissionDetector.State.IDLE
                ) {

                    saveCurrentTransmission();
                }

                if (
                        currentState == TransmissionDetector.State.IDLE
                ) {

                    preBuffer.add(
                            buffer,
                            bytesRead
                    );
                }

                printMeter(
                        rms,
                        currentState
                );
            }

        } finally {

            running = false;
            line = null;

            if (activeLine.isRunning()) {
                activeLine.stop();
            }

            if (activeLine.isOpen()) {
                activeLine.close();
            }

            // Save a partial transmission if monitoring
            // was stopped while recording.
            saveCurrentTransmission();

            System.out.println();
            System.out.println(
                    "Monitoring stopped."
            );
        }
    }

    public void stop() {

        running = false;

        TargetDataLine activeLine = line;

        if (activeLine != null) {
            activeLine.stop();
            activeLine.close();
        }
    }

    public boolean isRunning() {
        return running;
    }

    private void saveCurrentTransmission() {

        Transmission transmission =
                recorder.stop();

        if (transmission != null) {

            repository.save(transmission);
            if (listener != null) {
                listener.onTransmissionSaved(transmission);
            }

            System.out.println();
            System.out.println(transmission);
        }
    }

    private double calculateRms(
            byte[] buffer,
            int length
    ) {

        double sum = 0.0;
        int sampleCount = 0;

        for (
                int i = 0;
                i < length - 1;
                i += 2
        ) {

            int low =
                    buffer[i] & 0xFF;

            int high =
                    buffer[i + 1];

            short sample =
                    (short) (
                            (high << 8)
                            | low
                    );

            double normalized =
                    sample / 32768.0;

            sum +=
                    normalized
                    * normalized;

            sampleCount++;
        }

        if (sampleCount == 0) {
            return 0.0;
        }

        return Math.sqrt(
                sum / sampleCount
        );
    }

    private void printMeter(
            double rms,
            TransmissionDetector.State state
    ) {

        int barCount =
                (int) (rms * 200);

        barCount =
                Math.min(
                        barCount,
                        50
                );

        String bar =
                "#".repeat(barCount);

        System.out.printf(
                "\rRMS: %.5f | %-50s | %-9s",
                rms,
                bar,
                state
        );
    }
}