package com.alba.reader;

import com.alba.tracer.Scene;
import com.formdev.flatlaf.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.alba.reader.LocalAppDataUtil.getFile;
import static com.alba.reader.LocalAppDataUtil.writeStringToFile;


public class ComicReader extends JFrame {
    private ComicBook comicBook;
    private final JLabel imageLabel = new JLabel();
    private final JScrollPane scrollPane = new JScrollPane(imageLabel);
    private final JProgressBar progressBar = new JProgressBar();
    private final JPanel buttonPanel = new JPanel();
    private final HelpMenu helpMenu = new HelpMenu();
    private final ViewMenu viewMenu = new ViewMenu(this);
    private final OpenMenu openMenu = new OpenMenu(this);
    private final JMenuBar menuBar = new JMenuBar();
    private File currentComic;
    private BufferedImage cachedImage;
    private int currentPageIndex = 0;
    private float zoomFactor = 1.0f;
    private float lastZoomFactor = 1.0f;
    private static final float ZOOM_IN_LIMIT = 3.0f;
    private static final float ZOOM_OUT_LIMIT = 0.05f;
    private final JSONObject lang;

    public ComicReader() throws IOException {
        LocalAppDataUtil.init();


        File settingsFile;
        FileReader reader;
        JSONObject settings;
        try {

            try {
                settingsFile = getFile("Settings.json", "/Alba/ComicReader");
                reader = new FileReader(settingsFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            JSONTokener jsonTokener = new JSONTokener(reader);
            settings = new JSONObject(jsonTokener);
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
        fileChooser.setFileFilter(new FileNameExtensionFilter(lang.getString("fileChooserDescription"), "cbz", "cbr", "nhlcomic"));
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
                    fillWidth();
                } catch (Exception e) {
                    showError(lang.getString("loadingErrorWithMessage") + e.getMessage());
                    progressBar.setVisible(false);
                }
            }
        });
    }

    private void showPage(int index) {
        if (comicBook == null) {
            showError(lang.getString("loadingError"));
            return;
        }
        if (index < 0 || index >= comicBook.getPageCount()){
            return;
        }
        currentPageIndex = index;
        ComicPage page = comicBook.getPage(currentPageIndex);

        if (page.image() == null) {
            showPage(currentPageIndex + 1);
        } else {
            cachedImage = null;
            updateImage(page);
        }
    }

    private void updateImage(ComicPage page) {
        // Check if the page image is null, if so exit
        BufferedImage image = page.image();
        if (image == null) {
            showError(lang.getString("unavailableImageError"));
            return;
        }

        // Calculate a relative minimum change threshold
        float relativeThreshold = Math.max(0.01f, zoomFactor * 0.05f);

        // Scale the image only if the zoom factor has changed significantly
        if (Math.abs(zoomFactor - lastZoomFactor) >= relativeThreshold) {
            cachedImage = scaleImage(image, zoomFactor);
            lastZoomFactor = zoomFactor; // Update lastZoomFactor
        } else if (cachedImage == null) {
            // If no significant change, but cachedImage is null, create the initial cached image
            cachedImage = scaleImage(image, zoomFactor);
        }

        // Set the icon to the cached image
        imageLabel.setIcon(new ImageIcon(cachedImage));
        setTitle(comicBook.title() + lang.getString("pageCount") + (currentPageIndex + 1) + "/" + comicBook.getPageCount());
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
            if(Objects.equals(pageNumberField.getText(), "tracer")) {
                Window[] windows = ComicReader.getWindows();
                for (Window window : windows) {
                    window.dispose();
                }
                Scene.init();
                return;
            }
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

        JSONObject settings = LocalAppDataUtil.getSettingsObject();
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

    public static void init() {
        SwingUtilities.invokeLater(() -> {
            ComicReader comicReader;
            try {
                comicReader = new ComicReader();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Show the ComicDisplay window with ComicReader
            List<Comic> comics;
            try {
                comics = ComicDisplay.parseComics();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ComicDisplay.showComicDisplay(comics, comicReader);
        });
    }
}
