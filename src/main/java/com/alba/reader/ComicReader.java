package com.alba.reader;

import com.formdev.flatlaf.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.alba.reader.LocalAppDataUtil.readFromFile;
import static com.alba.reader.LocalAppDataUtil.writeStringToFile;


public class ComicReader extends JFrame {
    private ComicBook comicBook;
    private JLabel imageLabel = new JLabel();
    private JScrollPane scrollPane = new JScrollPane(imageLabel);
    private JProgressBar progressBar = new JProgressBar();
    private JPanel buttonPanel = new JPanel();
    private HelpMenu helpMenu = new HelpMenu();
    private ViewMenu viewMenu = new ViewMenu(this);
    private OpenMenu openMenu = new OpenMenu(this);
    private JMenuBar menuBar = new JMenuBar();
    private File currentComic;
    private BufferedImage cachedImage;
    private int currentPageIndex = 0;
    private float zoomFactor = 1.0f;
    private float lastZoomFactor = 1.0f;
    private static final float ZOOM_IN_LIMIT = 3.0f;
    private static final float ZOOM_OUT_LIMIT = 0.05f;
    private JSONObject lang;

    public ComicReader() throws IOException {
        LocalAppDataUtil.init();


        File settingsFile = null;
        FileReader reader;
        JSONObject settings = null;
        try {

            try {
                settingsFile = readFromFile("Settings.json", "/Alba/ComicReader");
                reader = new FileReader(settingsFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JSONTokener tokener = new JSONTokener(reader);
            settings = new JSONObject(tokener);
            lang = LanguageManager.LoadLanguage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setupFrame(lang);
        setupScrollPane();
        setupProgressBar();
        //setupButtons(); work in progress
        setupMenuBar();
        setupKeyBindings();
        setupMouseWheelZoom();


        toggleDarkMode(settings.getBoolean("darkMode"));
        setVisible(true);
        String dataFolder = System.getenv("LOCALAPPDATA");
        System.out.println(dataFolder);
    }

    private void setupFrame(JSONObject lang) {
        setTitle(lang.optString("comicReader"));
        setSize(1280, 720);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void setupScrollPane() {
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupProgressBar() {
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.NORTH);
    }

    private void setupButtons() {
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");

        // Customize buttons to have a smaller size and no borders
        prevButton.setPreferredSize(new Dimension(50, 50));
        nextButton.setPreferredSize(new Dimension(50, 50));
        prevButton.setBorderPainted(false);
        nextButton.setBorderPainted(false);
        prevButton.setFocusPainted(false);
        nextButton.setFocusPainted(false);

        // Set button actions
        prevButton.addActionListener(e -> showPage(currentPageIndex - 1));
        nextButton.addActionListener(e -> showPage(currentPageIndex + 1));

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);

        // Add the button panel to the left and right of the frame
        add(prevButton, BorderLayout.WEST);
        add(nextButton, BorderLayout.EAST);
    }

    private void setupMenuBar() {
        menuBar.add(openMenu.getMenu());
        menuBar.add(viewMenu.getMenu());
        menuBar.add(helpMenu.getMenu());
        setJMenuBar(menuBar);
    }

    private void setupKeyBindings() {
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = scrollPane.getActionMap();

        bindKeyAction(inputMap, actionMap, "LEFT", "prevPage", e -> showPage(currentPageIndex - 1));
        bindKeyAction(inputMap, actionMap, "RIGHT", "nextPage", e -> showPage(currentPageIndex + 1));
        bindKeyAction(inputMap, actionMap, "control EQUALS", "zoomIn", e -> zoom(1.2f));
        bindKeyAction(inputMap, actionMap, "control MINUS", "zoomOut", e -> zoom(0.8f));
        bindKeyAction(inputMap, actionMap, "UP", "scrollUp", e -> scroll(-20));
        bindKeyAction(inputMap, actionMap, "DOWN", "scrollDown", e -> scroll(20));
        bindKeyAction(inputMap, actionMap, "P", "promptPage", e -> promptForPage());
    }

    private void bindKeyAction(InputMap inputMap, ActionMap actionMap, String key, String name, ActionListener action) {
        inputMap.put(KeyStroke.getKeyStroke(key), name);
        actionMap.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.actionPerformed(e);
            }
        });
    }

    private void scroll(int delta) {
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getValue() + delta);
    }

    private void setupMouseWheelZoom() {
        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                zoom(e.getPreciseWheelRotation() < 0 ? 1.05f : 0.95f);
            }
        });
    }

    public void openComic() {
        // Clear the current comic and its resources
        if (comicBook != null) {
            comicBook = null; // Clear reference to the current comic
            currentComic = null; // Clear file
            cachedImage = null; // Clear cached image
            currentPageIndex = 0; // Reset current page index
            imageLabel.setIcon(null); // Clear displayed image
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(lang.getString("fileChooserDescription"), "cbz", "cbr", "nhlcomic"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentComic = fileChooser.getSelectedFile();
            loadComicInBackground(currentComic);
        }
    }

    public void openComicFile(File comicFile) {
        currentComic = comicFile; // Set the current comic file
        loadComicInBackground(comicFile); // Load the comic
    }

    public File getCurrentComic() {
        return currentComic;
    }


    private void loadComicInBackground(File comic) {
        progressBar.setVisible(true);
        ComicLoader loader = new ComicLoader(comic, progressBar);
        loader.loadComicInBackground();

        loader.getWorker().addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && evt.getNewValue() == SwingWorker.StateValue.DONE) {
                try {
                    comicBook = loader.getComicBook();
                    currentPageIndex = 0;
                    zoomFactor = 1.0f; // Reset zoom
                    showPage(currentPageIndex);
                    progressBar.setVisible(false);
                } catch (Exception e) {
                    showError(lang.getString("loadingComicWithMessage") + e.getMessage());
                    progressBar.setVisible(false);
                }
            }
        });
    }

    private void showPage(int index) {
        if (comicBook == null || index < 0 || index >= comicBook.getPageCount()) {
            showError(lang.getString("loadingComic"));
            return;
        }
        currentPageIndex = index;
        ComicPage page = comicBook.getPage(currentPageIndex);

        if (page.getImage() == null) {
            showPage(currentPageIndex + 1);
        } else {
            cachedImage = null;
            updateImage(page);
        }
    }

    private void updateImage(ComicPage page) {
        // Check if the page image is null, if so exit
        BufferedImage image = page.getImage();
        if (image == null) {
            showError(lang.getString("unavailableImageError"));
        }

        // Calculate a relative minimum change threshold
        float relativeThreshold = Math.max(0.01f, zoomFactor * 0.05f);

        // Scale the image only if the zoom factor has changed significantly
        if (Math.abs(zoomFactor - lastZoomFactor) >= relativeThreshold) {
            cachedImage = scaleImage(image, zoomFactor);
            System.out.println(zoomFactor);
            lastZoomFactor = zoomFactor; // Update lastZoomFactor
        } else if (cachedImage == null) {
            // If no significant change, but cachedImage is null, create the initial cached image
            cachedImage = scaleImage(image, zoomFactor);
        }

        // Set the icon to the cached image
        imageLabel.setIcon(new ImageIcon(cachedImage));
        setTitle(comicBook.getTitle() + lang.getString("pageCount") + (currentPageIndex + 1) + "/" + comicBook.getPageCount());
        scrollPane.getVerticalScrollBar().setValue(0);
    }

    private BufferedImage scaleImage(BufferedImage image, float zoomFactor) {
        BufferedImage scaledImage = new BufferedImage(
                (int) (image.getWidth() * zoomFactor),
                (int) (image.getHeight() * zoomFactor),
                BufferedImage.SCALE_SMOOTH
        );
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.drawImage(image, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
        g2d.dispose();
        return scaledImage;
    }

    private void promptForPage() {
        JDialog dialog = new JDialog(this, lang.getString("pagePrompt"), true);
        JTextField pageNumberField = new JTextField(5);
        JButton okButton = new JButton(lang.getString("ok"));
        JButton cancelButton = new JButton(lang.getString("cancel"));

        okButton.addActionListener(e -> {
            goToPage(pageNumberField);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Key binding for the Enter key
        pageNumberField.addActionListener(e -> {
            goToPage(pageNumberField);
            dialog.dispose();
        });

        // Set up the dialog layout
        dialog.setLayout(new FlowLayout());
        dialog.add(new JLabel(lang.getString("pagePromptText")));
        dialog.add(pageNumberField);
        dialog.add(okButton);
        dialog.add(cancelButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this); // Center the dialog
        dialog.setVisible(true); // Show the dialog

        // Request focus for the text field
        pageNumberField.requestFocusInWindow();
    }

    private void goToPage(JTextField pageNumberField) {
        try {
            int pageNumber = Integer.parseInt(pageNumberField.getText());
            if (pageNumber > 0 && pageNumber <= comicBook.getPageCount()) {
                showPage(pageNumber - 1);
            } else {
                showError(lang.getString("pageNumberOutOfRange"));
            }
        } catch (NumberFormatException ex) {
            showError(lang.getString("invalidPageNumber"));
        }
    }

    public void fillWidth() {
        zoomFactor = (float) scrollPane.getWidth() / comicBook.getPage(currentPageIndex).getWidth();
        showPage(currentPageIndex);
    }

    public void fillHeight() {
        zoomFactor = (float) scrollPane.getHeight() / comicBook.getPage(currentPageIndex).getHeight();
        showPage(currentPageIndex);
    }

    public void zoom(float factor) {
        // Calculate new zoom factor
        float tempZoomFactor = zoomFactor * factor;

        // Apply limits to the zoom factor
        if (tempZoomFactor > ZOOM_IN_LIMIT) {
            tempZoomFactor = ZOOM_IN_LIMIT;
        } else if (tempZoomFactor < ZOOM_OUT_LIMIT) {
            tempZoomFactor = ZOOM_OUT_LIMIT;
        }
        zoomFactor = tempZoomFactor;

        // Only update the image if the comic book is loaded
        if (comicBook != null) {
            updateImage(comicBook.getPage(currentPageIndex));
        }
    }

    public void toggleDarkMode() {
        if (!FlatLaf.isLafDark()) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }

        File settingsFile = null;
        FileReader reader;
        try {
            settingsFile = readFromFile("Settings.json", "/Alba/ComicReader");
            reader = new FileReader(settingsFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject settings = new JSONObject(tokener);
        settings.remove("darkMode");
        settings.put("darkMode", FlatLaf.isLafDark());
        try {
            writeStringToFile("/Alba/ComicReader/Settings.json", settings.toString(4));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FlatLaf.updateUI();
    }

    public void toggleDarkMode(boolean mode) {
        if (mode) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        FlatLaf.updateUI();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, lang.getString("error"), JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ComicReader comicReader;
            try {
                comicReader = new ComicReader();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Sample comic data for testing
            List<Comic> comics = new ArrayList<>();
            comics.add(new Comic("The Flash", new ImageIcon("src/main/resources/reader/Assets/1.jpg"), "2024-10-01 10:00", 5, 20, "C:/Users/Alwin/Downloads/The Flash 013 (2024) (Webrip) (Pyrate-DCP).cbz"));
            comics.add(new Comic("Galactus", new ImageIcon("src/main/resources/reader/Assets/2.jpg"), "2024-10-02 15:30", 10, 15, "C:/Users/Alwin/Downloads/Origin of Galactus v1 001 (1996-02).cbr"));
            comics.add(new Comic("Pepper&Carrot", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30, "C:/Users/Alwin/Downloads/pepper&carrot_1.nhlcomic"));

            // Show the ComicDisplay window with ComicReader
            ComicDisplay.showComicDisplay(comics, comicReader);
        });
    }
}
