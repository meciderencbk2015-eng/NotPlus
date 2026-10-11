package com.notplus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class Mainframe extends JFrame {
    private final Database database;
    private final Preferences preferences;
    private final List<Note> notes;
    private final DefaultListModel<Note> noteListModel;
    private final JList<Note> noteList;
    private final JTextField titleField;
    private final JTextField categoryField;
    private final JTextField searchField;
    private final JTextArea contentArea;
    private final JLabel metadataLabel;
    private final JLabel statusLabel;
    private final JLabel countLabel;
    private final JComboBox<String> themeComboBox;
    private final JComboBox<String> soundComboBox;
    private Note activeNote;
    private String currentFilter = "";

    public Mainframe() {
        super("NotPlus");
        this.database = new Database();
        this.preferences = Preferences.userNodeForPackage(Mainframe.class);
        this.notes = new ArrayList<>(database.loadNotes());
        this.noteListModel = new DefaultListModel<>();
        this.noteList = new JList<>(noteListModel);
        this.titleField = new JTextField();
        this.categoryField = new JTextField();
        this.searchField = new JTextField();
        this.contentArea = new JTextArea();
        this.metadataLabel = new JLabel();
        this.statusLabel = new JLabel("Hazır");
        this.countLabel = new JLabel();
        this.themeComboBox = new JComboBox<>(new String[]{"Varsayılan", "Koyu", "Mavi", "Lavanta"});
        this.soundComboBox = new JComboBox<>(new String[]{"Kapalı", "Bip", "Ses tonu"});

        configureWindow();
        buildLayout();
        initializeNotes();
        applyTheme(loadThemePreference());
        applySoundPreference(loadSoundPreference());
        wireEvents();
    }

    private void configureWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel sidebar = createSidebar();
        JPanel editor = createEditorPanel();
        JPanel settingsPanel = createSettingsPanel();

        root.add(sidebar, BorderLayout.WEST);
        root.add(editor, BorderLayout.CENTER);
        root.add(settingsPanel, BorderLayout.EAST);

        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(8, 8));
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.setBackground(new Color(242, 244, 248));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 231), 1),
                new EmptyBorder(12, 12, 12, 12))
        );

        JLabel title = new JLabel("Notlar");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        JButton newNoteButton = new JButton("+ Yeni not");
        newNoteButton.setFocusPainted(false);
        newNoteButton.addActionListener(e -> createNewNote());

        searchField.setToolTipText("Not ara");
        searchField.putClientProperty("JTextField.placeholderText", "Ara...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 216, 224), 1),
                new EmptyBorder(6, 8, 6, 8))
        );

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(newNoteButton, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new BorderLayout(6, 6));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);
        countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchPanel.add(countLabel, BorderLayout.SOUTH);

        noteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        noteList.setCellRenderer(new NoteListRenderer());
        noteList.setBackground(new Color(255, 255, 255));
        noteList.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        noteList.setVisibleRowCount(12);

        JScrollPane noteScroll = new JScrollPane(noteList);
        noteScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel sidebarTop = new JPanel();
        sidebarTop.setLayout(new BoxLayout(sidebarTop, BoxLayout.Y_AXIS));
        sidebarTop.setOpaque(false);
        sidebarTop.add(header);
        sidebarTop.add(Box.createVerticalStrut(8));
        sidebarTop.add(searchPanel);

        sidebar.add(sidebarTop, BorderLayout.NORTH);
        sidebar.add(noteScroll, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel createEditorPanel() {
        JPanel editor = new JPanel(new BorderLayout(10, 10));
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 231), 1),
                new EmptyBorder(16, 16, 16, 16))
        );
        editor.setBackground(new Color(250, 250, 252));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        JLabel titleLabel = new JLabel("Başlık");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        form.add(titleLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        titleField.setFont(new Font("SansSerif", Font.BOLD, 20));
        form.add(titleField, constraints);

        JLabel categoryLabel = new JLabel("Kategori");
        categoryLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        form.add(categoryLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        categoryField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        form.add(categoryField, constraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton saveButton = new JButton("Kaydet");
        saveButton.addActionListener(e -> saveCurrentNote());
        JButton deleteButton = new JButton("Sil");
        deleteButton.addActionListener(e -> deleteCurrentNote());
        JButton pinButton = new JButton("📌 Sabitle");
        pinButton.addActionListener(e -> togglePinnedNote());

        actions.add(saveButton);
        actions.add(pinButton);
        actions.add(deleteButton);

        editor.add(form, BorderLayout.NORTH);

        contentArea.setFont(new Font("SansSerif", Font.PLAIN, 18));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 216, 224), 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JScrollPane contentScroll = new JScrollPane(contentArea);
        editor.add(contentScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        footer.add(metadataLabel, BorderLayout.WEST);
        footer.add(statusLabel, BorderLayout.EAST);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));
        editor.add(footer, BorderLayout.SOUTH);

        return editor;
    }

    private JPanel createSettingsPanel() {
        JPanel settings = new JPanel();
        settings.setPreferredSize(new Dimension(260, 0));
        settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
        settings.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 231), 1),
                new EmptyBorder(16, 16, 16, 16))
        );
        settings.setBackground(new Color(245, 247, 250));

        JLabel settingsTitle = new JLabel("Ayarlar");
        settingsTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        settingsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        settings.add(settingsTitle);
        settings.add(Box.createVerticalStrut(16));

        JLabel themeLabel = new JLabel("Arka plan");
        themeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settings.add(themeLabel);
        themeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        settings.add(themeComboBox);
        settings.add(Box.createVerticalStrut(16));

        JLabel audioLabel = new JLabel("Ses");
        audioLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settings.add(audioLabel);
        soundComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        settings.add(soundComboBox);
        settings.add(Box.createVerticalStrut(20));

        JButton applyButton = new JButton("Uygula");
        applyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyButton.addActionListener(e -> {
            saveThemePreference((String) themeComboBox.getSelectedItem());
            saveSoundPreference((String) soundComboBox.getSelectedItem());
            applyTheme((String) themeComboBox.getSelectedItem());
            applySoundPreference((String) soundComboBox.getSelectedItem());
            statusLabel.setText("Ayarlar kaydedildi");
        });
        settings.add(applyButton);
        settings.add(Box.createVerticalGlue());

        return settings;
    }

    private void initializeNotes() {
        refreshNoteList();
        if (notes.isEmpty()) {
            createNewNote();
        } else {
            selectNote(notes.get(0));
        }
    }

    private void wireEvents() {
        noteList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && noteList.getSelectedIndex() >= 0) {
                selectNote(noteList.getSelectedValue());
            }
        });

        DocumentListener autosaveListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateActiveNoteFields(); }
            @Override public void removeUpdate(DocumentEvent e) { updateActiveNoteFields(); }
            @Override public void changedUpdate(DocumentEvent e) { updateActiveNoteFields(); }
        };

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        titleField.getDocument().addDocumentListener(autosaveListener);
        categoryField.getDocument().addDocumentListener(autosaveListener);
        contentArea.getDocument().addDocumentListener(autosaveListener);
    }

    private void updateActiveNoteFields() {
        if (activeNote == null) {
            return;
        }
        activeNote.setTitle(titleField.getText());
        activeNote.setCategory(categoryField.getText());
        activeNote.setContent(contentArea.getText());
        activeNote.touch();
        metadataLabel.setText("Son güncelleme: " + activeNote.getUpdatedText());
    }

    private void applyFilter() {
        currentFilter = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        refreshNoteList();
        if (activeNote != null && matchesFilter(activeNote)) {
            noteList.setSelectedValue(activeNote, true);
        } else if (!noteListModel.isEmpty()) {
            selectNote(noteListModel.getElementAt(0));
        }
    }

    private boolean matchesFilter(Note note) {
        if (currentFilter.isEmpty()) {
            return true;
        }
        String title = note.getTitle() == null ? "" : note.getTitle().toLowerCase();
        String content = note.getContent() == null ? "" : note.getContent().toLowerCase();
        String category = note.getCategory() == null ? "" : note.getCategory().toLowerCase();
        return title.contains(currentFilter) || content.contains(currentFilter) || category.contains(currentFilter);
    }

    private void createNewNote() {
        Note note = new Note();
        note.setId(nextId());
        note.setTitle("Yeni not");
        note.setCategory("Genel");
        note.setContent("Not içeriğini buraya yazın...");
        notes.add(note);
        refreshNoteList();
        selectNote(note);
        saveNotes();
    }

    private int nextId() {
        return notes.stream().mapToInt(Note::getId).max().orElse(0) + 1;
    }

    private void selectNote(Note note) {
        if (note == null) {
            return;
        }
        activeNote = note;
        titleField.setText(note.getTitle());
        categoryField.setText(note.getCategory());
        contentArea.setText(note.getContent());
        metadataLabel.setText("Oluşturma: " + note.getCreatedAt().toLocalDate() + " • Son güncelleme: " + note.getUpdatedText());
        noteList.setSelectedValue(note, true);
    }

    private void saveCurrentNote() {
        if (activeNote == null) {
            return;
        }

        activeNote.setTitle(titleField.getText());
        activeNote.setCategory(categoryField.getText());
        activeNote.setContent(contentArea.getText());
        activeNote.touch();
        saveNotes();
        refreshNoteList();
        noteList.setSelectedValue(activeNote, true);
        statusLabel.setText("Not kaydedildi");
        playSound();
    }

    private void deleteCurrentNote() {
        if (activeNote == null) {
            return;
        }

        int selectedIndex = noteList.getSelectedIndex();
        notes.remove(activeNote);
        saveNotes();
        refreshNoteList();

        if (!notes.isEmpty()) {
            int nextIndex = Math.min(selectedIndex, notes.size() - 1);
            if (nextIndex >= 0 && nextIndex < noteListModel.getSize()) {
                selectNote(noteListModel.getElementAt(nextIndex));
            } else if (!noteListModel.isEmpty()) {
                selectNote(noteListModel.getElementAt(0));
            }
        } else {
            titleField.setText("");
            categoryField.setText("");
            contentArea.setText("");
            metadataLabel.setText("Not yok");
            activeNote = null;
        }
        statusLabel.setText("Not silindi");
    }

    private void togglePinnedNote() {
        if (activeNote == null) {
            return;
        }
        activeNote.setPinned(!activeNote.isPinned());
        saveNotes();
        refreshNoteList();
        noteList.setSelectedValue(activeNote, true);
        statusLabel.setText(activeNote.isPinned() ? "Not sabitlendi" : "Not sabitlemesi kaldırıldı");
    }

    private void refreshNoteList() {
        noteListModel.clear();
        List<Note> visibleNotes = new ArrayList<>();
        for (Note note : notes) {
            if (matchesFilter(note)) {
                visibleNotes.add(note);
            }
        }
        visibleNotes.sort((a, b) -> Boolean.compare(b.isPinned(), a.isPinned()));
        for (Note note : visibleNotes) {
            noteListModel.addElement(note);
        }
        countLabel.setText(visibleNotes.size() + "/" + notes.size());
    }

    private void saveNotes() {
        database.saveNotes(notes);
    }

    private void playSound() {
        String selected = (String) soundComboBox.getSelectedItem();
        if (selected == null || "Kapalı".equals(selected)) {
            return;
        }

        if ("Bip".equals(selected)) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        try {
            generateTone(700, 180);
        } catch (Exception ignored) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void generateTone(int hz, int millis) throws IOException {
        byte[] buffer = new byte[1];
        javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false);
        javax.sound.sampled.SourceDataLine line = null;
        try {
            line = javax.sound.sampled.AudioSystem.getSourceDataLine(format.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED ? format : new javax.sound.sampled.AudioFormat(8000f, 8, 1, true, false));
            line.open(format);
            line.start();
            int samples = (int) (format.getSampleRate() * millis / 1000.0);
            byte[] tone = new byte[samples];
            for (int i = 0; i < tone.length; i++) {
                double angle = i / (format.getSampleRate() / hz) * 2.0 * Math.PI;
                tone[i] = (byte) (Math.sin(angle) * 100);
            }
            line.write(tone, 0, tone.length);
            line.drain();
        } catch (Exception e) {
            throw new IOException("Ses oluşturulamadı", e);
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }

    private void applyTheme(String themeName) {
        Color bg = new Color(248, 250, 252);
        Color panel = new Color(242, 244, 248);
        Color text = new Color(35, 42, 54);
        switch (themeName) {
            case "Koyu" -> {
                bg = new Color(27, 31, 35);
                panel = new Color(45, 50, 57);
                text = new Color(235, 238, 241);
            }
            case "Mavi" -> {
                bg = new Color(225, 239, 255);
                panel = new Color(203, 226, 255);
                text = new Color(21, 43, 72);
            }
            case "Lavanta" -> {
                bg = new Color(245, 238, 255);
                panel = new Color(230, 220, 255);
                text = new Color(46, 32, 74);
            }
            default -> {
            }
        }

        SwingUtilities.updateComponentTreeUI(this);
        getContentPane().setBackground(bg);
        getContentPane().setForeground(text);
        for (Component component : getContentPane().getComponents()) {
            component.setBackground(panel);
            component.setForeground(text);
        }
        titleField.setBackground(new Color(255, 255, 255, 220));
        categoryField.setBackground(new Color(255, 255, 255, 220));
        contentArea.setBackground(new Color(255, 255, 255, 220));
        contentArea.setForeground(text);
        noteList.setBackground(new Color(255, 255, 255, 220));
        noteList.setForeground(text);
        metadataLabel.setForeground(text);
        statusLabel.setForeground(text);
        repaint();
    }

    private void applySoundPreference(String soundMode) {
        if (soundMode == null) {
            soundMode = "Kapalı";
        }
        soundComboBox.setSelectedItem(soundMode);
    }

    private String loadThemePreference() {
        return preferences.get("theme", "Varsayılan");
    }

    private String loadSoundPreference() {
        return preferences.get("sound", "Kapalı");
    }

    private void saveThemePreference(String themeName) {
        preferences.put("theme", themeName == null ? "Varsayılan" : themeName);
    }

    private void saveSoundPreference(String soundMode) {
        preferences.put("sound", soundMode == null ? "Kapalı" : soundMode);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mainframe frame = new Mainframe();
            frame.setVisible(true);
        });
    }

    private static class NoteListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Note note) {
                setText((note.isPinned() ? "📌 " : "") + note.getTitle());
                setToolTipText(note.getCategory() + " • " + note.getShortPreview());
                setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                setFont(new Font("SansSerif", Font.PLAIN, 15));
            }
            return this;
        }
    }
}
