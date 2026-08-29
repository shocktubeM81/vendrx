package ca.vendrx.audio;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransmissionRecorder {

    private final AudioFormat format;
    private final Path recordingsDirectory;

    private ByteArrayOutputStream audioBuffer;

    public TransmissionRecorder(AudioFormat format) {
        this.format = format;
        this.recordingsDirectory = Path.of("recordings");
    }

    public void start() {

        audioBuffer = new ByteArrayOutputStream();

        System.out.println();
        System.out.println("Recording started");
    }

    public void append(byte[] data, int length) {

        if (audioBuffer != null) {
            audioBuffer.write(data, 0, length);
        }
    }

    public void stop() {

        if (audioBuffer == null) {
            return;
        }

        try {

            Files.createDirectories(recordingsDirectory);

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(
                            "yyyy-MM-dd_HH-mm-ss"
                    )
            );

            Path file = recordingsDirectory.resolve(
                    timestamp + ".wav"
            );

            writeWaveFile(file, audioBuffer.toByteArray());

            System.out.println();
            System.out.println(
                    "Saved: " + file.toAbsolutePath()
            );

        } catch (IOException e) {

            System.err.println(
                    "Unable to save recording: "
                            + e.getMessage()
            );

        } finally {

            audioBuffer = null;
        }
    }

    private void writeWaveFile(
            Path file,
            byte[] audioData
    ) throws IOException {

        int channels = format.getChannels();
        int sampleRate = (int) format.getSampleRate();
        int bitsPerSample = format.getSampleSizeInBits();

        int byteRate =
                sampleRate * channels * bitsPerSample / 8;

        int blockAlign =
                channels * bitsPerSample / 8;

        int dataLength = audioData.length;
        int fileLength = 36 + dataLength;

        try (var output = Files.newOutputStream(file)) {

            // RIFF
            output.write("RIFF".getBytes());
            writeIntLE(output, fileLength);

            // WAVE
            output.write("WAVE".getBytes());

            // fmt
            output.write("fmt ".getBytes());
            writeIntLE(output, 16);
            writeShortLE(output, 1); // PCM
            writeShortLE(output, channels);
            writeIntLE(output, sampleRate);
            writeIntLE(output, byteRate);
            writeShortLE(output, blockAlign);
            writeShortLE(output, bitsPerSample);

            // data
            output.write("data".getBytes());
            writeIntLE(output, dataLength);
            output.write(audioData);
        }
    }

    private void writeIntLE(
            java.io.OutputStream output,
            int value
    ) throws IOException {

        output.write(value & 0xFF);
        output.write((value >> 8) & 0xFF);
        output.write((value >> 16) & 0xFF);
        output.write((value >> 24) & 0xFF);
    }

    private void writeShortLE(
            java.io.OutputStream output,
            int value
    ) throws IOException {

        output.write(value & 0xFF);
        output.write((value >> 8) & 0xFF);
    }
}