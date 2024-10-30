package com.alba.reader;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;

class ComicCellRenderer extends DefaultListCellRenderer {
    private static final int IMAGE_WIDTH = 180;  // Desired width for scaling
    private static final int IMAGE_HEIGHT = 320; // Desired height for scaling
    private final Map<ImageIcon, ImageIcon> imageCache = new HashMap<>();

    /**
     * @param list         The JList we're painting.
     * @param value        The value returned by list.getModel().getElementAt(index).
     * @param index        The cells index.
     * @param isSelected   True if the specified cell was selected.
     * @param cellHasFocus True if the specified cell has the focus.
     * @return comicPanel
     */
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof Comic comic) {
            JSONObject lang;
            try {
                lang = LanguageManager.LoadLanguage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
            JLabel subtitleLabel = new JLabel(lang.getString("lastOpenedCol") + comic.lastOpened);
            JLabel pageLabel = new JLabel(lang.getString("pageCount2") + (comic.currentPage + 1) + lang.getString("of") + comic.totalPages);

            textPanel.add(titleLabel);
            textPanel.add(subtitleLabel);
            textPanel.add(pageLabel);

            panel.add(textPanel, BorderLayout.SOUTH);

            panel.setPreferredSize(new Dimension(254 + 20, IMAGE_HEIGHT + 60));

            return panel;
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

    /**Handels caching icons
     * @param icon The image icon
     * @return ImageIcon
     */
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