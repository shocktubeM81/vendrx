package ca.vendrx.audio;

import ca.vendrx.model.Transmission;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class AudioInputMonitor {

    private static final int AUDIO_BUFFER_SIZE = 4096;

    private final Mixer.Info mixerInfo;
    private final AudioFormat format;
    private final TransmissionProcessor transmissionProcessor;
    private final UnaryOperator<Transmission> transmissionSaver;
    private final AudioMonitorListener listener;

    private volatile boolean running;
    private volatile TargetDataLine line;

    public AudioInputMonitor(
            Mixer.Info mixerInfo,
            AudioFormat format,
            TransmissionProcessor transmissionProcessor,
            UnaryOperator<Transmission> transmissionSaver,
            AudioMonitorListener listener) {
        this.mixerInfo = Objects.requireNonNull(mixerInfo);
        this.format = Objects.requireNonNull(format);
        this.transmissionProcessor = Objects.requireNonNull(transmissionProcessor);
        this.transmissionSaver = Objects.requireNonNull(transmissionSaver);
        this.listener = listener;
    }

    public void start() throws LineUnavailableException {
        TargetDataLine activeLine = openInputLine();
        line = activeLine;
        running = true;

        printMonitoringStarted();

        try {
            monitorAudio(activeLine);
        } finally {
            closeInputLine(activeLine);
            saveTransmission(transmissionProcessor.finish());
            System.out.println("\nMonitoring stopped.");
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

    private TargetDataLine openInputLine() throws LineUnavailableException {
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        DataLine.Info lineInfo = new DataLine.Info(TargetDataLine.class, format);
        TargetDataLine activeLine = (TargetDataLine) mixer.getLine(lineInfo);
        activeLine.open(format);
        activeLine.start();
        return activeLine;
    }

    private void monitorAudio(TargetDataLine activeLine) {
        byte[] audioBuffer = new byte[AUDIO_BUFFER_SIZE];

        while (running) {
            int bytesRead = readAudio(activeLine, audioBuffer);
            if (bytesRead <= 0) {
                continue;
            }

            double rms = PcmRmsCalculator.calculate16BitLittleEndian(audioBuffer, bytesRead);
            Transmission completedTransmission = transmissionProcessor.process(
                    audioBuffer,
                    bytesRead,
                    rms);

            saveTransmission(completedTransmission);
            printMeter(rms, transmissionProcessor.getState());
        }
    }

    private int readAudio(TargetDataLine activeLine, byte[] audioBuffer) {
        try {
            return activeLine.read(audioBuffer, 0, audioBuffer.length);
        } catch (IllegalStateException e) {
            if (!running) {
                return -1;
            }
            throw e;
        }
    }

    private void saveTransmission(Transmission transmission) {
        if (transmission == null) {
            return;
        }

        Transmission savedTransmission = transmissionSaver.apply(transmission);
        if (listener != null) {
            listener.onTransmissionSaved(savedTransmission);
        }

        System.out.println();
        System.out.println(savedTransmission);
    }

    private void closeInputLine(TargetDataLine activeLine) {
        running = false;
        line = null;

        if (activeLine.isRunning()) {
            activeLine.stop();
        }
        if (activeLine.isOpen()) {
            activeLine.close();
        }
    }

    private void printMonitoringStarted() {
        System.out.println();
        System.out.println("Monitoring: " + mixerInfo.getName());
        System.out.println("Ctrl+C to stop.");
        System.out.println();
    }

    private void printMeter(double rms, TransmissionDetector.State state) {
        int barCount = Math.min((int) (rms * 200), 50);
        String bar = "#".repeat(barCount);

        System.out.printf(
                "\rRMS: %.5f | %-50s | %-9s",
                rms,
                bar,
                state);
    }
}
