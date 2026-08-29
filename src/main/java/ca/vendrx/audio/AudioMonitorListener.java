package ca.vendrx.audio;

import ca.vendrx.model.Transmission;

public interface AudioMonitorListener {

    void onTransmissionSaved(
            Transmission transmission
    );
}