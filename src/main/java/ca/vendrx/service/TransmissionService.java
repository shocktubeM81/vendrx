package ca.vendrx.service;

import ca.vendrx.database.TransmissionRepository;
import ca.vendrx.model.Transmission;

import java.io.IOException;
import java.nio.file.Files;

public class TransmissionService {

    private final TransmissionRepository repository;

    public TransmissionService(
            TransmissionRepository repository
    ) {

        this.repository =
                repository;
    }

    public void delete(
            Transmission transmission
    ) {

        if (transmission == null) {
            throw new IllegalArgumentException(
                    "Transmission cannot be null."
            );
        }

        try {

            Files.deleteIfExists(
                    transmission.getFilePath()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to delete WAV file.",
                    e
            );
        }

        repository.delete(
                transmission
        );
    }
}