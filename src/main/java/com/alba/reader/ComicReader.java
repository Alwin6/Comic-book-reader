package com.alba.reader;

import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ComicReader extends JFrame {
    private ComicBook comicBook;
    private JLabel imageLabel;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    private JPanel buttonPanel = new JPanel();
    private HelpMenu helpMenu = new HelpMenu();
    private ViewMenu viewMenu = new ViewMenu(this);
    private OpenMenu openMenu = new OpenMenu(this);
    private JMenuBar menuBar = new JMenuBar();
    private int currentPageIndex = 0;
    private float zoomFactor = 1.0f;

    public ComicReader() {
        setTitle("Comic Reader");
        setSize(1280, 720); // prevents the window from just being the title bar when not maximized
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        scrollPane = new JScrollPane(imageLabel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        add(progressBar, BorderLayout.NORTH);
        progressBar.setVisible(false);

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

        setupKeyBindings();

        // Mouse Wheel Listener for Zooming
        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                // Use precise wheel rotation for better accuracy
                double rotation = e.getPreciseWheelRotation();

                // Zoom in if the wheel is rotated up (negative rotation)
                if (rotation < 0) {
                    zoom(1.05f);
                }
                // Zoom out if the wheel is rotated down (positive rotation)
                else if (rotation > 0) {
                    zoom(0.95f);
                }
            }
        });

        menuBar.add(openMenu.getMenu());
        menuBar.add(viewMenu.getMenu());
        menuBar.add(helpMenu.getMenu());
        setJMenuBar(menuBar);

        toggleDarkMode();

        setVisible(true);
    }

    private void setupKeyBindings() {
        InputMap inputMap = scrollPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = scrollPane.getActionMap();

        // Navigate to previous page
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "prevPage");
        actionMap.put("prevPage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPage(currentPageIndex - 1);
            }
        });

        // Navigate to next page
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "nextPage");
        actionMap.put("nextPage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPage(currentPageIndex + 1);
            }
        });

        // Zoom In
        inputMap.put(KeyStroke.getKeyStroke("control EQUALS"), "zoomIn");
        actionMap.put("zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoom(1.2f);
            }
        });

        // Zoom Out
        inputMap.put(KeyStroke.getKeyStroke("control MINUS"), "zoomOut");
        actionMap.put("zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoom(0.8f);
            }
        });

        // Scroll Up
        inputMap.put(KeyStroke.getKeyStroke("UP"), "scrollUp");
        actionMap.put("scrollUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getValue() - 20); // Scroll up by 20 pixels
            }
        });

        // Scroll Down
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "scrollDown");
        actionMap.put("scrollDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                verticalScrollBar.setValue(verticalScrollBar.getValue() + 20); // Scroll down by 20 pixels
            }
        });
    }

    public void openComic() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CBZ, CBR And Nhl files", "cbz","cbr","nhlcomic"));
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadComicInBackground(file);

        }
    }

    private void loadComicInBackground(File file) {
        progressBar.setVisible(true);
        ComicLoader loader = new ComicLoader(file, progressBar);
        loader.loadComicInBackground();

        loader.getWorker().addPropertyChangeListener(evt -> {
            if ("state".equals(evt.getPropertyName()) && evt.getNewValue() == SwingWorker.StateValue.DONE) {
                try {
                    comicBook = loader.getComicBook(); // Get the loaded comic book
                    currentPageIndex = 0;
                    zoomFactor = 1.0f; // Reset zoom
                    showPage(currentPageIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error loading comic: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void showPage(int index) {
        if (comicBook == null || index < 0 || index >= comicBook.getPageCount()) {
            JOptionPane.showMessageDialog(this, "Error loading comic", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        currentPageIndex = index;
        ComicPage page = comicBook.getPage(currentPageIndex);

        if (page.getImage() == null) {
            currentPageIndex++;
            showPage(currentPageIndex);
        } else {
            updateImage(page);
        }
    }

    private void updateImage(ComicPage page) {
        ImageIcon icon;

        // Simply scale based on the current zoom factor
        icon = new ImageIcon(page.getImage().getScaledInstance(
                (int) (page.getWidth() * zoomFactor),
                (int) (page.getHeight() * zoomFactor),
                Image.SCALE_SMOOTH));

        imageLabel.setIcon(icon);
        setTitle(comicBook.getTitle() + " - Page " + (currentPageIndex + 1) + "/" + comicBook.getPageCount());
        scrollPane.getVerticalScrollBar().setValue(0);
    }
    private void goToPage(JTextField pageNumberField) {
        try {
            int pageNumber = Integer.parseInt(pageNumberField.getText());
            if (pageNumber > 0 && pageNumber <= comicBook.getPageCount()) {
                showPage(pageNumber - 1);
            } else {
                JOptionPane.showMessageDialog(this, "Page number out of range.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid page number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void fillWidth() {
        zoomFactor = (float) scrollPane.getWidth() / comicBook.getPage(currentPageIndex).getWidth(); // Calculate zoom factor
        showPage(currentPageIndex); // Refresh to apply fill width
    }

    public void fillHeight() {
        zoomFactor = (float) scrollPane.getHeight() / comicBook.getPage(currentPageIndex).getHeight(); // Calculate zoom factor
        showPage(currentPageIndex); // Refresh to apply fill height
    }

    public void zoom(float factor) {
        zoomFactor *= factor;
        // Update the currently displayed page to reflect new zoom
        updateImage(comicBook.getPage(currentPageIndex));
    }

    public void toggleDarkMode() {
        if (!FlatLaf.isLafDark()) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
        FlatLaf.updateUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ComicReader::new);
    }
}
