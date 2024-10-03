package com.alba.reader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.awt.Image;

public class ComicDisplay extends JFrame {
    private JPanel comicPanel;

    public ComicDisplay(List<Comic> comics) {
        setTitle("Comic Library");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        comicPanel = new JPanel();
        comicPanel.setLayout(new GridLayout(0, 5, 10, 10)); // 3 columns with 10px gap
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

        return panel;
    }

    private ImageIcon scaleImage(ImageIcon icon, int width, int height) {
        Image img = icon.getImage(); // transform it
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // scale it
        return new ImageIcon(scaledImg); // transform it back
    }

    public static void main(String[] args) {
        // Sample comic data
        List<Comic> comics = new ArrayList<>();
        comics.add(new Comic("Comic 1", new ImageIcon("src/main/resources/reader/Assets/1.jpg"), "2024-10-01 10:00", 5, 20));
        comics.add(new Comic("Comic 2", new ImageIcon("src/main/resources/reader/Assets/2.jpg"), "2024-10-02 15:30", 10, 15));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));
        comics.add(new Comic("Comic 3", new ImageIcon("src/main/resources/reader/Assets/3.jpg"), "2024-10-03 12:00", 1, 30));

        // Add more comics as needed

        SwingUtilities.invokeLater(() -> new ComicDisplay(comics));
    }
}

