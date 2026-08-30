package ca.vendrx.audio;

public class TransmissionDetector {

    public enum State {
        IDLE,
        RECORDING
    }

    private final double threshold;
    private final long silenceTimeoutMs;

    private State state = State.IDLE;

    private long lastSignalTime = 0;

    public TransmissionDetector(
            double threshold,
            long silenceTimeoutMs) {
        this.threshold = threshold;
        this.silenceTimeoutMs = silenceTimeoutMs;
    }

    public void update(double rms) {

        long now = System.currentTimeMillis();

        boolean signal = rms >= threshold;

        switch (state) {

            case IDLE -> {

                if (signal) {

                    state = State.RECORDING;
                    lastSignalTime = now;

                    System.out.println();
                    System.out.println(">>> Transmission started");
                }
            }

            case RECORDING -> {

                if (signal) {

                    lastSignalTime = now;

                } else if (now - lastSignalTime >= silenceTimeoutMs) {

                    state = State.IDLE;

                    System.out.println();
                    System.out.println("<<< Transmission ended");
                }
            }
        }
    }

    public State getState() {
        return state;
    }
}