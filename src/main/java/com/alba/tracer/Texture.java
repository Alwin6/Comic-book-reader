package com.alba.tracer;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Texture {
    private BufferedImage image;
    private boolean hasTexture;

    // Constructor for loading texture from a file
    public Texture(String fileName) {
        try {
            this.image = ImageIO.read(new File("src/main/resources/tracer/Assets/" + fileName));
            this.hasTexture = true; // Indicate that this texture has an image loaded
        } catch (IOException e) {
            System.err.println("Error loading texture file: " + fileName);
            this.image = null;
            this.hasTexture = false; // Failed to load, treat as no texture
        }
    }

    // Constructor for no texture (default)
    public Texture() {
        this.image = null;
        this.hasTexture = false; // Indicate that there is no texture
    }

    // Method to check if this object has a valid texture
    public boolean hasTexture() {
        return hasTexture;
    }

    // Method to sample color from texture at UV coordinates (u, v)
    public Vector3 sample(double u, double v) {
        
        if (!hasTexture || image == null) {
            // Return a default color (e.g., white) if there's no texture
            return new Vector3(1.0, 1.0, 1.0);
        }

        // Wrap UV coordinates to ensure they are within [0, 1]
        u = u % 1.0;
        v = v % 1.0;
        if (u < 0) u += 1.0;
        if (v < 0) v += 1.0;

        // Map UV coordinates to pixel coordinates
        int x = (int) (u * (image.getWidth() - 1));
        int y = (int) ((1 - v) * (image.getHeight() - 1)); // Flip vertically for correct orientation

        // Get the pixel color
        int rgb = image.getRGB(x, y);

        // Extract RGB components and convert to a Vector3
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        // Convert 0-255 range to 0.0-1.0 range
        return new Vector3(r / 255.0, g / 255.0, b / 255.0);
    }
}
