package com.alba.reader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ComicReaderGUI extends JFrame {
    private ComicBook comicBook;
    private JLabel imageLabel;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    private int currentPageIndex = 0;
    private float zoomFactor = 1.0f;
    private HelpMenu helpMenu;

    public ComicReaderGUI() {
        setTitle("Comic Reader");
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        scrollPane = new JScrollPane(imageLabel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        add(progressBar, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        JButton prevButton = new JButton("Previous");
        JButton nextButton = new JButton("Next");
        JButton openButton = new JButton("Open Comic");
        JButton zoomInButton = new JButton("Zoom In");
        JButton zoomOutButton = new JButton("Zoom Out");

        buttonPanel.add(openButton);
        buttonPanel.add(prevButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(zoomInButton);
        buttonPanel.add(zoomOutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        openButton.addActionListener(e -> openComic());
        prevButton.addActionListener(e -> showPage(currentPageIndex - 1));
        nextButton.addActionListener(e -> showPage(currentPageIndex + 1));
        zoomInButton.addActionListener(e -> zoom(1.2f)); // Zoom in by 20%
        zoomOutButton.addActionListener(e -> zoom(0.8f)); // Zoom out by 20%

        setupKeyBindings();

        // Mouse Wheel Listener for Zooming
        scrollPane.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                // Zoom in if the wheel is rotated up (positive)
                if (e.getWheelRotation() < 0) {
                    zoom(1.05f);
                }
                // Zoom out if the wheel is rotated down (negative)
                else {
                    zoom(0.95f);
                }
            }
        });

        // Setup Help Menu
        helpMenu = new HelpMenu();
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(helpMenu.getMenu());
        setJMenuBar(menuBar);

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

    private void openComic() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CBZ and CBR files", "cbz","cbr"));
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadComicInBackground(file);

        }
    }

    private void loadComicInBackground(File file) {
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
            return;
        }
        progressBar.setIndeterminate(false);
        currentPageIndex = index;
        ComicPage page = comicBook.getPage(currentPageIndex);
        if (page.getImage() == null){
            currentPageIndex++;
            showPage(currentPageIndex);
        }
        else {
            ImageIcon icon = new ImageIcon(page.getImage().getScaledInstance(
                    (int) (page.getWidth() * zoomFactor),
                    (int) (page.getHeight() * zoomFactor),
                    Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);
            setTitle(comicBook.getTitle() + " - Page " + (currentPageIndex));
            scrollPane.getVerticalScrollBar().setValue(0);
        }

    }

    private void zoom(float factor) {
        zoomFactor *= factor;
        showPage(currentPageIndex); // Refresh the displayed page with new zoom level
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ComicReaderGUI::new);
    }
}
