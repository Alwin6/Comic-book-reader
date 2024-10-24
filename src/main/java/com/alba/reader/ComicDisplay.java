package com.alba.reader;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ComicDisplay extends JFrame {
    private final ComicReader comicReader; // Reference to ComicReader

    public ComicDisplay(List<Comic> comics, ComicReader comicReader) {
        this.comicReader = comicReader;

        setTitle("Comic Display");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Only close this window
        setSize(800, 600);

        // Create the JList
        JList<Comic> comicList = new JList<>(new DefaultListModel<>());
        DefaultListModel<Comic> model = (DefaultListModel<Comic>) comicList.getModel();
        for (Comic comic : comics) {
            model.addElement(comic);
        }

        // Set custom cell renderer
        comicList.setCellRenderer(new ComicCellRenderer());
        comicList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        comicList.setVisibleRowCount(-1);
        //comicList.setFixedCellHeight(100); // Adjust height as needed

        // Add mouse listener for click events
        comicList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = comicList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Comic selectedComic = model.getElementAt(index);
                    onComicSelected(selectedComic.filePath); // Call the method to handle the click
                }
            }
        });

        // Add the list to a scroll pane
        JScrollPane scrollPane = new JScrollPane(comicList);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private void onComicSelected(String filePath) {
        File comicFile = new File(filePath);
        if (comicReader != null) {
            comicReader.openComicFile(comicFile); // Open the comic in ComicReader
        } else {
            // Handle the case where comicReader is null if necessary
            JOptionPane.showMessageDialog(this, "Comic Reader is not available.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<Comic> parseComics() throws IOException {
        // Read the file contents
        String content = new String(Files.readAllBytes(LocalAppDataUtil.getFile("ComicList.json", "/Alba/ComicReader").toPath()));

        // Parse the content
        JSONObject jsonObject = new JSONObject(content);
        ArrayList<Comic> comicsList = new ArrayList<>();

        for (String key : jsonObject.keySet()) {
            JSONObject comicJson = jsonObject.getJSONObject(key);
            String path = comicJson.getString("path");
            boolean read = comicJson.getBoolean("read");
            long lastOpened = comicJson.getLong("lastOpened");
            int currentPage = comicJson.getInt("currentPage");
            int totalPages = getTotalPages(path);
            ImageIcon thumbnail = getThumbnail(path);
            String title = key; // Using the key as the title

            // Create a new Comic object
            Comic comic = new Comic(title, thumbnail, read, lastOpened, currentPage, totalPages, path);
            comicsList.add(comic);
        }

        // Return the list
        return comicsList;
    }

    private static int getTotalPages(String path) throws IOException {
        int totalPages = 0;
        File comicFile = new File(path);
        if (FileTypeDetector.isZip(path)) {
            totalPages = ComicBookZip.unzip(comicFile, Arrays.asList("jpg", "jpeg", "png", "gif")).size();
        }else if (FileTypeDetector.isRar(path)) {
            totalPages = ComicBookRar.getMatchingEntries(comicFile, Arrays.asList("jpg", "jpeg", "png", "gif")).size();
        }

        return totalPages;
    }

    private static ImageIcon getThumbnail(String path) throws IOException {
        ImageIcon thumbnail;
        File comicFile = new File(path);
        if (FileTypeDetector.isZip(path)) {
            ZipEntry thumbnailEntry = ComicBookZip.unzip(comicFile, Arrays.asList("jpg", "jpeg", "png", "gif")).getFirst();
            ZipFile zip = new ZipFile(comicFile);
            InputStream is = zip.getInputStream(thumbnailEntry);
            thumbnail = new ImageIcon(ImageIO.read(is));
        }else if (FileTypeDetector.isRar(path)) {
            thumbnail = new ImageIcon(ComicBookRar.extractFirstImage(comicFile));
        }else{
            thumbnail = new ImageIcon("src/main/resources/reader/Assets/1.jpg");
        }
        return thumbnail;
    }

    public static void showComicDisplay(List<Comic> comics, ComicReader comicReader) {
        SwingUtilities.invokeLater(() -> new ComicDisplay(comics, comicReader));
    }
}
