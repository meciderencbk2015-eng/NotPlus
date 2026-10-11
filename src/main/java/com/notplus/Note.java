package com.notplus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private int id;
    private String title;
    private String content;
    private String category;
    private boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Note() {
        this(0, "Yeni not", "", "Genel", false, LocalDateTime.now(), LocalDateTime.now());
    }

    public Note(String title, String content) {
        this(0, title, content, "Genel", false, LocalDateTime.now(), LocalDateTime.now());
    }

    public Note(int id, String title, String content, String category, boolean pinned, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title == null || title.isBlank() ? "Yeni not" : title;
        this.content = content == null ? "" : content;
        this.category = category == null || category.isBlank() ? "Genel" : category;
        this.pinned = pinned;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null || title.isBlank() ? "Yeni not" : title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category == null || category.isBlank() ? "Genel" : category;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getShortPreview() {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            return "Not içeriği yok";
        }
        String compact = trimmed.replaceAll("\\s+", " ");
        return compact.length() > 80 ? compact.substring(0, 77) + "..." : compact;
    }

    public String getUpdatedText() {
        return updatedAt == null ? "-" : updatedAt.format(FORMATTER);
    }

    @Override
    public String toString() {
        return title;
    }
}
