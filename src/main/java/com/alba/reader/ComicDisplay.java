package com.alba.reader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.awt.Image;

public class ComicDisplay extends JFrame {
    private JPanel comicPanel;
    private ComicReader comicReader; // Reference to ComicReader

    public ComicDisplay(List<Comic> comics, ComicReader comicReader) {
        this.comicReader = comicReader; // Store the reference
        setTitle("Comic Library");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Allow reopening
        setLayout(new BorderLayout());

        comicPanel = new JPanel();
        comicPanel.setLayout(new GridLayout(0, 5, 10, 10)); // 5 columns with 10px gap
        JScrollPane scrollPane = new JScrollPane(comicPanel);
        add(scrollPane, BorderLayout.CENTER);

        for (Comic comic : comics) {
            comicPanel.add(createComicPanel(comic));
        }

        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
    }

    private JPanel createComicPanel(Comic comic) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(comic.title);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel thumbnailLabel = new JLabel(scaleImage(comic.thumbnail, 200, 300)); // Resize to fit the panel
        thumbnailLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel infoLabel = new JLabel("Last Opened: " + comic.lastOpened +
                " | Page " + comic.currentPage + " of " + comic.totalPages);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(thumbnailLabel, BorderLayout.NORTH);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);

        // Add mouse listener to the panel
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onComicSelected(comic.filePath); // Call your function with the file path
            }
        });

        return panel;
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

    private ImageIcon scaleImage(ImageIcon icon, int width, int height) {
        Image img = icon.getImage(); // transform it
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // scale it
        return new ImageIcon(scaledImg);
    }

    public static void showComicDisplay(List<Comic> comics, ComicReader comicReader) {
        SwingUtilities.invokeLater(() -> new ComicDisplay(comics, comicReader));
    }
}
