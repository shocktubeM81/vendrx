package ca.vendrx.config;

import javax.sound.sampled.AudioFormat;

public class AudioConfig {

    private final float sampleRate;
    private final int sampleSizeBits;
    private final int channels;

    private final double signalThreshold;
    private final long silenceTimeoutMs;
    private final int preBufferSeconds;

    public AudioConfig(
            float sampleRate,
            int sampleSizeBits,
            int channels,
            double signalThreshold,
            long silenceTimeoutMs,
            int preBufferSeconds) {
        this.sampleRate = sampleRate;
        this.sampleSizeBits = sampleSizeBits;
        this.channels = channels;
        this.signalThreshold = signalThreshold;
        this.silenceTimeoutMs = silenceTimeoutMs;
        this.preBufferSeconds = preBufferSeconds;
    }

    public static AudioConfig defaultConfig() {

        return new AudioConfig(
                44100.0f,
                16,
                1,
                0.20,
                3000,
                2);
    }

    public AudioFormat createAudioFormat() {

        return new AudioFormat(
                sampleRate,
                sampleSizeBits,
                channels,
                true,
                false);
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public int getSampleSizeBits() {
        return sampleSizeBits;
    }

    public int getChannels() {
        return channels;
    }

    public double getSignalThreshold() {
        return signalThreshold;
    }

    public long getSilenceTimeoutMs() {
        return silenceTimeoutMs;
    }

    public int getPreBufferSeconds() {
        return preBufferSeconds;
    }
}