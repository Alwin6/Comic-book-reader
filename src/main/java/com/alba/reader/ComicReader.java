package com.alba.reader;

import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.awt.image.BufferedImage;

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

    public ComicReader() {
        setupFrame();
        setupScrollPane();
        setupProgressBar();
        setupButtons();
        setupMenu();
        setupKeyBindings();
        setupMouseWheelZoom();

        toggleDarkMode();
        setVisible(true);
    }

    private void setupFrame() {
        setTitle("Comic Reader");
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
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JTextField pageNumberField = new JTextField(5);
        JButton goToPageButton = new JButton("Go to Page");

        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(new JLabel("Page:"));
        buttonPanel.add(pageNumberField);
        buttonPanel.add(goToPageButton);
        add(buttonPanel, BorderLayout.SOUTH);

        prevButton.addActionListener(e -> showPage(currentPageIndex - 1));
        nextButton.addActionListener(e -> showPage(currentPageIndex + 1));
        goToPageButton.addActionListener(e -> goToPage(pageNumberField));
    }

    private void setupMenu() {
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
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CBZ, CBR And Nhl files", "cbz", "cbr", "nhlcomic"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentComic = fileChooser.getSelectedFile();
            loadComicInBackground(currentComic);
        }
    }

    public File getCurrentComic() {
        return currentComic;
    }

    private void loadComicInBackground(File file) {
        progressBar.setVisible(true);
        ComicLoader loader = new ComicLoader(file, progressBar);
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
                    showError("Error loading comic: " + e.getMessage());
                    progressBar.setVisible(false);
                }
            }
        });
    }

    private void showPage(int index) {
        if (comicBook == null || index < 0 || index >= comicBook.getPageCount()) {
            showError("Error loading comic");
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
            showError("Image not available for this page.");
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
        setTitle(comicBook.getTitle() + " - Page " + (currentPageIndex + 1) + "/" + comicBook.getPageCount());
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


    private void goToPage(JTextField pageNumberField) {
        try {
            int pageNumber = Integer.parseInt(pageNumberField.getText());
            if (pageNumber > 0 && pageNumber <= comicBook.getPageCount()) {
                showPage(pageNumber - 1);
            } else {
                showError("Page number out of range.");
            }
        } catch (NumberFormatException ex) {
            showError("Please enter a valid page number.");
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
        FlatLaf.updateUI();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ComicReader::new);
    }
}
