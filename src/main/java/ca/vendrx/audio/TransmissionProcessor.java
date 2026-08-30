package ca.vendrx.audio;

import ca.vendrx.model.Transmission;

import java.util.Objects;

public final class TransmissionProcessor {

    private final TransmissionDetector detector;
    private final TransmissionRecorder recorder;
    private final PreBuffer preBuffer;

    public TransmissionProcessor(
            TransmissionDetector detector,
            TransmissionRecorder recorder,
            PreBuffer preBuffer) {
        this.detector = Objects.requireNonNull(detector);
        this.recorder = Objects.requireNonNull(recorder);
        this.preBuffer = Objects.requireNonNull(preBuffer);
    }

    public Transmission process(byte[] audio, int length, double rms) {
        TransmissionDetector.State previousState = detector.getState();
        detector.update(rms);
        TransmissionDetector.State currentState = detector.getState();

        if (startedRecording(previousState, currentState)) {
            startRecordingWithPreBuffer();
        }

        if (currentState == TransmissionDetector.State.RECORDING) {
            recorder.append(audio, length);
            recorder.addRms(rms);
        }

        Transmission completedTransmission = null;
        if (stoppedRecording(previousState, currentState)) {
            completedTransmission = recorder.stop();
        }

        if (currentState == TransmissionDetector.State.IDLE) {
            preBuffer.add(audio, length);
        }

        return completedTransmission;
    }

    public Transmission finish() {
        return recorder.stop();
    }

    public TransmissionDetector.State getState() {
        return detector.getState();
    }

    private void startRecordingWithPreBuffer() {
        recorder.start();
        byte[] bufferedAudio = preBuffer.getAudio();
        recorder.append(bufferedAudio, bufferedAudio.length);
    }

    private boolean startedRecording(
            TransmissionDetector.State previousState,
            TransmissionDetector.State currentState) {
        return previousState == TransmissionDetector.State.IDLE
                && currentState == TransmissionDetector.State.RECORDING;
    }

    private boolean stoppedRecording(
            TransmissionDetector.State previousState,
            TransmissionDetector.State currentState) {
        return previousState == TransmissionDetector.State.RECORDING
                && currentState == TransmissionDetector.State.IDLE;
    }
}
