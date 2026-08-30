package ca.vendrx.service;

import ca.vendrx.database.TransmissionRepository;
import ca.vendrx.model.Transmission;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public final class TransmissionService {

    private final TransmissionRepository repository;

    public TransmissionService(TransmissionRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Transmission> findRecent(int limit) {
        return repository.findRecent(limit);
    }

    public void delete(Transmission transmission) {
        Objects.requireNonNull(transmission, "Transmission cannot be null.");

        try {
            Files.deleteIfExists(transmission.getFilePath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete WAV file.", e);
        }

        repository.delete(transmission);
    }
}
