package ca.vendrx.audio;

public class PreBuffer {

    private final byte[] buffer;

    private int writePosition = 0;
    private int size = 0;

    public PreBuffer(int capacityBytes) {

        if (capacityBytes <= 0) {
            throw new IllegalArgumentException(
                    "capacityBytes must be greater than 0");
        }

        buffer = new byte[capacityBytes];
    }

    public void add(byte[] data, int length) {

        for (int i = 0; i < length; i++) {

            buffer[writePosition] = data[i];

            writePosition = (writePosition + 1) % buffer.length;

            if (size < buffer.length) {
                size++;
            }
        }
    }

    public byte[] getAudio() {

        byte[] result = new byte[size];

        int start = (writePosition - size + buffer.length)
                % buffer.length;

        for (int i = 0; i < size; i++) {

            result[i] = buffer[(start + i) % buffer.length];
        }

        return result;
    }

    public int size() {
        return size;
    }
}