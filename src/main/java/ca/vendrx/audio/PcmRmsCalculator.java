package ca.vendrx.audio;

final class PcmRmsCalculator {

    private static final double PCM_16_SCALE = 32768.0;

    private PcmRmsCalculator() {
    }

    static double calculate16BitLittleEndian(byte[] audio, int length) {
        if (length < 0 || length > audio.length) {
            throw new IllegalArgumentException("length must be within the audio buffer.");
        }

        double sumOfSquares = 0.0;
        int sampleCount = 0;

        for (int index = 0; index + 1 < length; index += 2) {
            int lowByte = audio[index] & 0xFF;
            int highByte = audio[index + 1];
            short sample = (short) ((highByte << 8) | lowByte);
            double normalizedSample = sample / PCM_16_SCALE;

            sumOfSquares += normalizedSample * normalizedSample;
            sampleCount++;
        }

        return sampleCount == 0
                ? 0.0
                : Math.sqrt(sumOfSquares / sampleCount);
    }
}
