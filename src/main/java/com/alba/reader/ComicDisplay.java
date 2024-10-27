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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ComicDisplay extends JFrame {
    private final ComicReader comicReader; // Reference to ComicReader
    private final JList<Comic> comicList;

    public ComicDisplay(List<Comic> comics, ComicReader comicReader) {
        this.comicReader = comicReader;
        JSONObject lang;
        try {
            lang = LanguageManager.LoadLanguage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setTitle(lang.getString("comicDisplay"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Only close this window
        setSize(1125, 825);
        setLocationRelativeTo(null);

        // Search sort and filter panels
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel searchLabel = new JLabel("  " + lang.getString("search"));

        JTextField search = new JTextField();

        JLabel sort = new JLabel(" " + lang.getString("sort"));

        JComboBox<String> sortBy = new JComboBox<>();
        sortBy.addItem(lang.getString("lastOpened"));
        sortBy.addItem(lang.getString("title"));
        sortBy.addItem(lang.getString("pages"));
        sortBy.setSelectedItem(lang.getString("lastOpened"));

        JComboBox<String> sortOrder = new JComboBox<>();
        sortOrder.addItem(lang.getString("ascending"));
        sortOrder.addItem(lang.getString("descending"));
        sortOrder.setSelectedItem(lang.getString("descending"));

        JLabel filter = new JLabel(" " + lang.getString("filter"));

        JComboBox<String> filterBy = new JComboBox<>();
        filterBy.addItem(lang.getString("none"));
        filterBy.addItem(lang.getString("read"));
        filterBy.addItem(lang.getString("notRead"));
        filterBy.addItem(lang.getString("reading"));
        filterBy.addItem(lang.getString("favorite"));
        filterBy.addItem(lang.getString("notFavorite"));
        filterBy.setSelectedItem(lang.getString("none"));

        JButton go = new JButton(lang.getString("goSearch"));

        go.addActionListener(e -> performSearchAction(comics, sortBy, sortOrder, filterBy, search, lang));
        search.addActionListener(e -> performSearchAction(comics, sortBy, sortOrder, filterBy, search, lang));

        panel.add(searchLabel);
        panel.add(search, gbc);
        panel.add(sort);
        panel.add(sortBy);
        panel.add(sortOrder);
        panel.add(filter);
        panel.add(filterBy);
        panel.add(go);
        panel.add(new JLabel("  "));
        add(panel, BorderLayout.NORTH); // Add search and filter segment

        // Initialize comicList without thumbnails
        DefaultListModel<Comic> model = new DefaultListModel<>();
        for (Comic comic : comics) {
            // Create a comic with a placeholder image
            comic.setThumbnail(new ImageIcon("src/main/resources/reader/Assets/placeholder.jpg")); // Placeholder image
            model.addElement(comic);
        }
        this.comicList = new JList<>(model);

        // Set custom cell renderer
        comicList.setCellRenderer(new ComicCellRenderer());
        comicList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        comicList.setVisibleRowCount(-1);

        // Add mouse listener for click events
        comicList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) { // Right click, options
                    int index = comicList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Comic selectedComic = model.getElementAt(index);
                        JPopupMenu contextMenu = new JPopupMenu();

                        JMenuItem open = new JMenuItem(lang.getString("open"));
                        JMenuItem rename = new JMenuItem(lang.getString("rename"));
                        JMenuItem delete = new JMenuItem(lang.getString("remove"));
                        JSeparator separator = new JSeparator();
                        JMenuItem favorite = new JMenuItem(lang.getString("markFav"));
                        if (selectedComic.favorite) {
                            favorite = new JMenuItem(lang.getString("markFavNot"));
                        }
                        JMenuItem read = new JMenuItem(lang.getString("markRead"));
                        if (selectedComic.read) {
                            read = new JMenuItem(lang.getString("markUnread"));
                        }

                        open.addActionListener(g -> {
                            onComicSelected(selectedComic.filePath); // Call the method to handle the click
                        });

                        rename.addActionListener(g -> {
                           JDialog renameDialog = new JDialog();
                           renameDialog.setTitle(lang.getString("rename"));
                           JPanel panel = new JPanel();

                           JLabel label = new JLabel(lang.getString("renameCol"));
                           JTextField name = new JTextField();
                           name.setText(selectedComic.title);
                           name.setColumns(32);

                           panel.add(label);
                           panel.add(name);

                           name.addActionListener(h -> {
                               ComicListManager.rename(selectedComic.ID, name.getText());
                               model.getElementAt(index).title = name.getText();
                               performSearchAction(comics, sortBy, sortOrder, filterBy, search, lang);
                               renameDialog.dispose();
                           });

                           renameDialog.setResizable(false);

                           renameDialog.getContentPane().add(panel);
                           renameDialog.pack();
                           renameDialog.setLocationRelativeTo(null);
                           renameDialog.setVisible(true);
                        });

                        delete.addActionListener(g -> {
                            ComicListManager.remove(selectedComic.ID);
                            comics.remove(index);
                            performSearchAction(comics, sortBy, sortOrder, filterBy, search, lang);
                        });

                        favorite.addActionListener(g -> {
                            boolean newValue = !(boolean)ComicListManager.readField(selectedComic.ID, "favorite");
                            ComicListManager.updateField(selectedComic.ID, "favorite", newValue);
                            model.getElementAt(index).favorite = newValue;
                            performSearchAction(comics, sortBy, sortOrder, filterBy, search, lang);
                        });
                        read.addActionListener(g -> {
                            boolean newValue = !(boolean)ComicListManager.readField(selectedComic.ID, "read");
                            ComicListManager.updateField(selectedComic.ID, "read", newValue);
                            model.getElementAt(index).read = newValue;
                            performSearchAction(comics, sortBy, sortOrder, filterBy, search, lang);
                        });

                        contextMenu.add(open);
                        contextMenu.add(rename);
                        contextMenu.add(delete);
                        contextMenu.add(separator);
                        contextMenu.add(favorite);
                        contextMenu.add(read);

                        contextMenu.show(comicList, e.getX(), e.getY());
                    }


                } else { // Left click, open comic
                    int index = comicList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Comic selectedComic = model.getElementAt(index);
                        onComicSelected(selectedComic.filePath); // Call the method to handle the click
                    }
                }
            }
        });

        // Add the list to a scroll pane
        JScrollPane scrollPane = new JScrollPane(comicList);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
        SwingUtilities.invokeLater(() -> getContentPane().requestFocusInWindow());

        // Load thumbnails in the background
        loadThumbnails(comics);
    }

    private void loadThumbnails(List<Comic> comics) {
        SwingWorker<Void, Comic> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                for (Comic comic : comics) {
                    try {
                        // Load the thumbnail
                        ImageIcon thumbnail = getThumbnail(comic.getFilePath());
                        comic.setThumbnail(thumbnail);
                        publish(comic); // Publish the comic with the loaded thumbnail
                    } catch (IOException e) {
                        e.printStackTrace(); // Handle exceptions as needed
                    }
                }
                return null;
            }

            @Override
            protected void process(List<Comic> chunks) {
                DefaultListModel<Comic> model = (DefaultListModel<Comic>) comicList.getModel();
                for (Comic comic : chunks) {
                    model.setElementAt(comic, model.indexOf(comic)); // Update the model with the loaded thumbnail
                }
            }

            @Override
            protected void done() {
                // Any final updates after loading
            }
        };
        worker.execute();
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
        // You know there's LoadComicList for that, right?
        String content = new String(Files.readAllBytes(LocalAppDataUtil.getFile("ComicList.json", "/Alba/ComicReader").toPath()));

        // Parse the content
        JSONObject jsonObject = new JSONObject(content);
        ArrayList<Comic> comicsList = new ArrayList<>();

        for (String key : jsonObject.keySet()) {
            JSONObject comicJson = jsonObject.getJSONObject(key);
            String path = comicJson.getString("path");
            boolean read = comicJson.getBoolean("read");
            boolean favorite = comicJson.getBoolean("favorite");
            long lastOpened = comicJson.getLong("lastOpened");
            int currentPage = comicJson.getInt("currentPage");
            int totalPages = getTotalPages(path);
            ImageIcon thumbnail = getThumbnail(path);
            String title;

            JSONObject metadata = comicJson.getJSONObject("metadata");
            if (metadata.has("title")) {
                title = metadata.getString("title");
            } else if (metadata.has("Title")) {
                title = metadata.getString("Title");
            } else if (metadata.has("name")) {
                title = metadata.getString("name");
            } else if (metadata.has("Name")) {
                title = metadata.getString("Name");
            } else {
                title = key; // Using the key as the title if there wasn't a title in the metadata
            }

            // Create a new Comic object
            Comic comic = new Comic(title, thumbnail, read, favorite, lastOpened, currentPage, totalPages, path, metadata, key);
            comicsList.add(comic);
        }

        ComicRefinement.sortComics(comicsList, "Last opened", false); // Default order

        // Return the list
        return comicsList;
    }

    public void updateComics(List<Comic> newComics) {
        DefaultListModel<Comic> model = (DefaultListModel<Comic>) comicList.getModel();
        model.clear(); // Clear the existing list
        for (Comic comic : newComics) {
            model.addElement(comic); // Add new comics
        }
    }

    private static int getTotalPages(String path) throws IOException {
        int totalPages;
        File comicFile = new File(path);
        if (FileTypeDetector.isNhl(path)) {
            totalPages = ComicBookNhl.getImagesFromGifInZip(comicFile).size();
        }else if (FileTypeDetector.isZip(path)) {
            totalPages = ComicBookZip.unzip(comicFile, Arrays.asList("jpg", "jpeg", "png", "gif")).size();
        }else if (FileTypeDetector.isRar(path)) {
            totalPages = ComicBookRar.getMatchingEntries(comicFile, Arrays.asList("jpg", "jpeg", "png", "gif")).size();
        }else{
            totalPages = 0;
        }

        return totalPages;
    }

    private static ImageIcon getThumbnail(String path) throws IOException {
        ImageIcon thumbnail;
        File comicFile = new File(path);
        if(FileTypeDetector.isNhl(path)) {
            thumbnail = new ImageIcon(ComicBookNhl.getImagesFromGifInZip(comicFile).getLast());
        }else if (FileTypeDetector.isZip(path)) {
            ZipEntry thumbnailEntry = ComicBookZip.unzip(comicFile, Arrays.asList("jpg", "jpeg", "png", "gif")).getFirst();
            ZipFile zip = new ZipFile(comicFile);
            InputStream is = zip.getInputStream(thumbnailEntry);
            thumbnail = new ImageIcon(ImageIO.read(is));
        }else if (FileTypeDetector.isRar(path)) {
            thumbnail = new ImageIcon(ComicBookRar.extractFirstImage(comicFile));
        }else{
            thumbnail = new ImageIcon("src/main/resources/reader/Assets/notfound.png");
        }
        return thumbnail;
    }

    public static void showComicDisplay(List<Comic> comics, ComicReader comicReader) {
        SwingUtilities.invokeLater(() -> new ComicDisplay(comics, comicReader));
    }

    private void performSearchAction(List<Comic> comics, JComboBox<String> sortBy, JComboBox<String> sortOrder,
                                     JComboBox<String> filterBy, JTextField search, JSONObject lang) {
        List<Comic> newComics;

        Map<String, String> sortMap = Map.of(
                lang.getString("lastOpened"), "Last opened",
                lang.getString("title"), "Title",
                lang.getString("pages"), "Pages"
        );
        String sortValue = sortMap.get((String)sortBy.getSelectedItem());
        newComics = ComicRefinement.sortComics(comics, sortValue, sortOrder.getSelectedItem() == lang.getString("ascending"));

        Map<String, String> filterMap = Map.of(
                lang.getString("read"), "Read",
                lang.getString("notRead"), "Not read",
                lang.getString("reading"), "Reading",
                lang.getString("favorite"), "Favorite",
                lang.getString("notFavorite"), "Not favorite"
        );

        String filterValue = filterMap.get((String)filterBy.getSelectedItem());
        if (filterValue != null) {
            newComics = ComicRefinement.filterComics(newComics, filterValue);
        }

        newComics = ComicRefinement.searchComics(newComics, search.getText());

        updateComics(newComics);
    }

}
