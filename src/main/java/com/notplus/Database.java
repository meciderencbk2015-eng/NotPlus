package com.notplus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final Path storagePath;

    public Database() {
        this(Paths.get(System.getProperty("user.home"), ".notplus", "notes.ser"));
    }

    Database(Path storagePath) {
        this.storagePath = storagePath;
    }

    public void saveNotes(List<Note> notes) {
        try {
            Path parent = storagePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(storagePath))) {
                outputStream.writeObject(new ArrayList<>(notes));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Notlar kaydedilemedi.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Note> loadNotes() {
        if (!Files.exists(storagePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(storagePath))) {
            Object value = inputStream.readObject();
            if (value instanceof List<?>) {
                List<Note> results = new ArrayList<>();
                for (Object item : (List<?>) value) {
                    if (item instanceof Note) {
                        results.add((Note) item);
                    }
                }
                return results;
            }
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }

        return new ArrayList<>();
    }
}
