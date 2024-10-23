package com.alba.reader;

import javax.swing.*;
import java.awt.*;
import java.util.*;

class ComicCellRenderer extends DefaultListCellRenderer {
    private static final int IMAGE_WIDTH = 180;  // Desired width for scaling
    private static final int IMAGE_HEIGHT = 320; // Desired height for scaling
    private final Map<ImageIcon, ImageIcon> imageCache = new HashMap<>();

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Comic comic) {

            // Create a JPanel to hold the image and text
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            if (comic.read) {
                panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2)); // Green border if read
            } else {
                panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Padding
            }

            // Retrieve or scale the image
            ImageIcon scaledIcon = getScaledIcon(comic.thumbnail);
            JLabel imageLabel = new JLabel(scaledIcon);
            imageLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageLabel.setVerticalAlignment(JLabel.CENTER);
            panel.add(imageLabel, BorderLayout.CENTER);

            // Create a JPanel for the text
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel(comic.title);
            JLabel subtitleLabel = new JLabel("Last Opened: " + comic.lastOpened + " | Page " + comic.currentPage + " of " + comic.totalPages);

            textPanel.add(titleLabel);
            textPanel.add(subtitleLabel);

            panel.add(textPanel, BorderLayout.SOUTH);

            panel.setPreferredSize(new Dimension(subtitleLabel.getPreferredSize().width + 20, IMAGE_HEIGHT + 60));

            return panel;
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    private ImageIcon getScaledIcon(ImageIcon icon) {
        // Check if the scaled image is already cached
        if (imageCache.containsKey(icon)) {
            return imageCache.get(icon);
        }

        // Scale the image
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        // Cache the scaled image
        imageCache.put(icon, scaledIcon);
        return scaledIcon;
    }
}