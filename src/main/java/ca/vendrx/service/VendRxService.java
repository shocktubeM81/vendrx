package ca.vendrx.service;

import ca.vendrx.audio.AudioInputMonitor;
import ca.vendrx.audio.AudioMonitorListener;
import ca.vendrx.audio.PreBuffer;
import ca.vendrx.audio.TransmissionDetector;
import ca.vendrx.audio.TransmissionRecorder;
import ca.vendrx.audio.AudioMonitorListener;
import ca.vendrx.config.AudioConfig;
import ca.vendrx.database.TransmissionRepository;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

public class VendRxService {

    private final AudioConfig audioConfig;
    private final TransmissionRepository repository;

    private AudioInputMonitor monitor;
    private Thread monitoringThread;
    private AudioMonitorListener listener;


    public void setAudioMonitorListener(
            AudioMonitorListener listener
    ) {
        this.listener = listener;
    }

    public VendRxService(
            AudioConfig audioConfig,
            TransmissionRepository repository
    ) {
        this.audioConfig = audioConfig;
        this.repository = repository;
    }

    public synchronized void startMonitoring(
            Mixer.Info mixerInfo
    ) {

        if (isMonitoring()) {
            throw new IllegalStateException(
                    "VendRx is already monitoring."
            );
        }

        AudioFormat format =
                audioConfig.createAudioFormat();

        TransmissionDetector detector =
                new TransmissionDetector(
                        audioConfig.getSignalThreshold(),
                        audioConfig.getSilenceTimeoutMs()
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
                        bytesPerSecond
                        * audioConfig.getPreBufferSeconds()
                );

        monitor =
                new AudioInputMonitor(
                        mixerInfo,
                        format,
                        detector,
                        recorder,
                        preBuffer,
                        repository,
                        listener
                );

        monitoringThread =
                new Thread(
                        this::runMonitor,
                        "vendrx-audio-monitor"
                );

        monitoringThread.start();
    }

    public synchronized void stopMonitoring() {

        if (monitor != null) {
            monitor.stop();
        }
    }

    public synchronized boolean isMonitoring() {

        return monitoringThread != null
                && monitoringThread.isAlive();
    }

    private void runMonitor() {

        try {

            monitor.start();

        } catch (LineUnavailableException e) {

            System.err.println(
                    "Unable to open audio input: "
                    + e.getMessage()
            );

        } finally {

            synchronized (this) {
                monitor = null;
                monitoringThread = null;
            }
        }
    }
}