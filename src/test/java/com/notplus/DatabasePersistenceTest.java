package com.notplus;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabasePersistenceTest {

    @Test
    void saveAndLoadNotes_shouldPersistRoundTrip() throws Exception {
        Path file = Files.createTempFile("notplus", ".ser");
        Database database = new Database(file);

        Note note = new Note();
        note.setId(7);
        note.setTitle("Yapılacaklar");
        note.setCategory("İş");
        note.setContent("1. Raporu hazırla\n2. E-postayı gönder");
        note.setCreatedAt(LocalDateTime.of(2026, 10, 10, 9, 15));
        note.setUpdatedAt(LocalDateTime.of(2026, 10, 10, 9, 30));

        database.saveNotes(List.of(note));

        List<Note> loaded = database.loadNotes();

        assertEquals(1, loaded.size());
        assertEquals("Yapılacaklar", loaded.get(0).getTitle());
        assertEquals("İş", loaded.get(0).getCategory());
        assertEquals("1. Raporu hazırla\n2. E-postayı gönder", loaded.get(0).getContent());
    }
}
