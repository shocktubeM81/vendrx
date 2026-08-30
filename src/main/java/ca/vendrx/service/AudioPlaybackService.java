package ca.vendrx.service;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.IOException;
import java.nio.file.Path;

public class AudioPlaybackService {

    private Clip clip;
    private Runnable onPlaybackStopped;

    public synchronized void play(Path filePath)
            throws IOException,
            UnsupportedAudioFileException,
            LineUnavailableException {

        stop();

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                filePath.toFile());

        Clip newClip = AudioSystem.getClip();

        newClip.addLineListener(event -> {

            if (event.getType() == LineEvent.Type.STOP) {

                synchronized (AudioPlaybackService.this) {

                    if (clip == newClip
                            && newClip.getFramePosition() >= newClip.getFrameLength()) {

                        newClip.close();
                        clip = null;

                        notifyPlaybackStopped();
                    }
                }
            }
        });

        try {
            newClip.open(audioStream);
        } finally {
            audioStream.close();
        }

        clip = newClip;
        clip.start();
    }

    public synchronized void stop() {

        if (clip == null) {
            return;
        }

        Clip currentClip = clip;
        clip = null;

        currentClip.stop();
        currentClip.close();

        notifyPlaybackStopped();
    }

    public synchronized boolean isPlaying() {

        return clip != null
                && clip.isRunning();
    }

    public void setOnPlaybackStopped(
            Runnable onPlaybackStopped) {

        this.onPlaybackStopped = onPlaybackStopped;
    }

    private void notifyPlaybackStopped() {

        if (onPlaybackStopped != null) {
            onPlaybackStopped.run();
        }
    }
}