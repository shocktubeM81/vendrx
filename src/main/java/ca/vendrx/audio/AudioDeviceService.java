package ca.vendrx.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;

public class AudioDeviceService {

    public List<Mixer.Info> getInputDevices() {

        List<Mixer.Info> inputs = new ArrayList<>();

        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {

            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            for (Line.Info lineInfo : mixer.getTargetLineInfo()) {

                if (TargetDataLine.class.isAssignableFrom(
                        lineInfo.getLineClass()
                )) {

                    inputs.add(mixerInfo);
                    break;
                }
            }
        }

        return inputs;
    }
}