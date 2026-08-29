package ca.vendrx.audio;

import javax.sound.sampled.*;

public class AudioInputMonitor {

    private static final float SAMPLE_RATE = 44100.0f;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;

    private final Mixer.Info mixerInfo;
    private final TransmissionDetector transmissionDetector;
    private final TransmissionRecorder recorder;
    private final PreBuffer preBuffer;

    public AudioInputMonitor(
        Mixer.Info mixerInfo,
        TransmissionDetector transmissionDetector,
        TransmissionRecorder recorder,
        PreBuffer preBuffer
    ) {
        this.mixerInfo = mixerInfo;
        this.transmissionDetector = transmissionDetector;
        this.recorder = recorder;
        this.preBuffer = preBuffer;
    }

    public void start() throws LineUnavailableException {

        AudioFormat format = new AudioFormat(
                SAMPLE_RATE,
                SAMPLE_SIZE_BITS,
                CHANNELS,
                true,
                false
        );

        Mixer mixer = AudioSystem.getMixer(mixerInfo);

        DataLine.Info lineInfo = new DataLine.Info(
                TargetDataLine.class,
                format
        );

        TargetDataLine line =
                (TargetDataLine) mixer.getLine(lineInfo);

        line.open(format);
        line.start();

        System.out.println();
        System.out.println("Monitoring: " + mixerInfo.getName());
        System.out.println("Ctrl+C to stop.");
        System.out.println();

        byte[] buffer = new byte[4096];

        while (true) {

            int bytesRead = line.read(
                    buffer,
                    0,
                    buffer.length
            );

            double rms = calculateRms(buffer, bytesRead);

            TransmissionDetector.State previousState =
                    transmissionDetector.getState();

            transmissionDetector.update(rms);

            TransmissionDetector.State currentState =
                    transmissionDetector.getState();

            if (previousState == TransmissionDetector.State.IDLE
                    && currentState == TransmissionDetector.State.RECORDING) {

                recorder.start();

                byte[] bufferedAudio =
                        preBuffer.getAudio();

                recorder.append(
                        bufferedAudio,
                        bufferedAudio.length
                );
            }

            if (currentState == TransmissionDetector.State.RECORDING) {

                recorder.append(
                        buffer,
                        bytesRead
                );
            }

            if (previousState == TransmissionDetector.State.RECORDING
                    && currentState == TransmissionDetector.State.IDLE) {

                recorder.stop();
            }

            if (currentState == TransmissionDetector.State.IDLE) {

                preBuffer.add(
                        buffer,
                        bytesRead
                );
            }

            printMeter(rms, currentState);
        }
    }

    private double calculateRms(byte[] buffer, int length) {

        double sum = 0.0;
        int sampleCount = 0;

        for (int i = 0; i < length - 1; i += 2) {

            int low = buffer[i] & 0xFF;
            int high = buffer[i + 1];

            short sample =
                    (short) ((high << 8) | low);

            double normalized =
                    sample / 32768.0;

            sum += normalized * normalized;

            sampleCount++;
        }

        if (sampleCount == 0) {
            return 0.0;
        }

        return Math.sqrt(sum / sampleCount);
    }

    private void printMeter(
            double rms,
            TransmissionDetector.State state
    ) {

        int barCount = (int) (rms * 200);
        barCount = Math.min(barCount, 50);

        String bar = "#".repeat(barCount);

        System.out.printf(
                "\rRMS: %.5f | %-50s | %-9s",
                rms,
                bar,
                state
        );
    }
}