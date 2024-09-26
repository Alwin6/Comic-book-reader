package com.alba.tracer;

import java.io.*;
import java.util.Arrays;

public class HDRLoader {

    private float[][][] imageData; // 3D array to store RGB values
    private int width;
    private int height;
    public String filename;

    public HDRLoader(String filePath) throws IOException {
        this.filename = filePath;
        loadHDR(filePath);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void loadHDR(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            // Read and parse the header
            StringBuilder header = new StringBuilder();
            boolean formatFound = false;
            int b;
            
            while ((b = inputStream.read()) != -1) {
                char c = (char) b;
                header.append(c);
                if (c == '\n') {
                    String line = header.toString().trim();
                    System.out.println("Header line: " + line);
                    header.setLength(0); // Clear the buffer

                    if (line.startsWith("#")) continue; // Skip comments
                    if (line.startsWith("FORMAT=32-bit_rle_rgbe")) {
                        formatFound = true;
                    } else if (line.startsWith("-Y")) {
                        // Resolution line found, parse it
                        String[] resolution = line.split(" ");
                        height = Integer.parseInt(resolution[1]);
                        width = Integer.parseInt(resolution[3]);
                        System.out.println("Image dimensions: Width = " + width + ", Height = " + height);
                        break;
                    }
                }
            }

            if (!formatFound) {
                throw new IOException("HDR file format not supported or not found.");
            }

            if (width <= 0 || height <= 0) {
                throw new IOException("Invalid image dimensions.");
            }

            imageData = new float[height][width][3]; // Initialize imageData array

            // Read pixel data
            byte[] rgbeBuffer = new byte[4];
            for (int y = 0; y < height; y++) {
                if (inputStream.read(rgbeBuffer) != 4) {
                    throw new IOException("Failed to read HDR file.");
                }

                // Check for RLE encoded scanline
                if (rgbeBuffer[0] != 2 || rgbeBuffer[1] != 2 || (rgbeBuffer[2] & 0x80) != 0) {
                    // Non-RLE encoded scanline
                    decodeNonRLEScanline(rgbeBuffer, inputStream, y);
                    continue;
                }

                // RLE encoded scanline
                int scanlineWidth = ((rgbeBuffer[2] & 0xFF) << 8) | (rgbeBuffer[3] & 0xFF);
                if (scanlineWidth != width) {
                    throw new IOException("Scanline width does not match image width.");
                }

                byte[] scanline = new byte[4 * width];
                for (int i = 0; i < 4; i++) { // Read 4 channels (R, G, B, E)
                    int pos = 0;
                    while (pos < width) {
                        int value = inputStream.read();
                        if (value == -1) {
                            throw new IOException("Unexpected end of file while reading scanline.");
                        }
                        if (value > 128) { // Run length encoding
                            int count = value - 128;
                            value = inputStream.read();
                            if (value == -1) {
                                throw new IOException("Unexpected end of file while reading RLE data.");
                            }
                            Arrays.fill(scanline, i * width + pos, i * width + pos + count, (byte) value);
                            pos += count;
                        } else { // Raw bytes
                            int readBytes = inputStream.read(scanline, i * width + pos, value);
                            if (readBytes != value) {
                                throw new IOException("Unexpected end of file while reading raw data.");
                            }
                            pos += value;
                        }
                    }
                }

                // Convert RGBE to RGB floats
                convertScanlineToRGB(scanline, y);
            }
        }
    }

    private void decodeNonRLEScanline(byte[] rgbeBuffer, InputStream inputStream, int y) throws IOException {
        byte[] scanline = new byte[4 * width]; // 4 channels per pixel
        System.arraycopy(rgbeBuffer, 0, scanline, 0, 4); // Copy the first pixel data

        // Read the rest of the scanline
        int readBytes = inputStream.read(scanline, 4, (width - 1) * 4);
        if (readBytes != (width - 1) * 4) {
            throw new IOException("Failed to read full scanline data.");
        }

        // Convert RGBE to RGB floats
        convertScanlineToRGB(scanline, y);
    }

    private void convertScanlineToRGB(byte[] scanline, int y) {
        for (int x = 0; x < width; x++) {
            int r = scanline[x] & 0xFF;
            int g = scanline[width + x] & 0xFF;
            int b = scanline[2 * width + x] & 0xFF;
            int e = scanline[3 * width + x] & 0xFF;
            if (e == 0) {
                imageData[y][x][0] = 0;
                imageData[y][x][1] = 0;
                imageData[y][x][2] = 0;
            } else {
                float scale = (float) Math.pow(2.0f, e - 128.0f) / 255.0f;
                imageData[y][x][0] = r * scale;
                imageData[y][x][1] = g * scale;
                imageData[y][x][2] = b * scale;
            }
        }
    }

    public Vector3 sample(double u, double v) {
        int x = (int) (u * (width - 1));
        int y = (int) ((1.0 - v) * (height - 1));  // Invert y for correct orientation

        // Ensure x and y are within bounds
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        float[] pixel = imageData[y][x];  // Access the HDR data
        return new Vector3(pixel[0], pixel[1], pixel[2]);  // Convert to Vector3
    }

    // For testing purposes
    public static void main(String[] args) {
        try {
            HDRLoader hdrLoader = new HDRLoader("tracer/Assets/autumn_field_puresky_2k.hdr");
            System.out.println("Loaded HDR image with width: " + hdrLoader.getWidth() + " and height: " + hdrLoader.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
