package com.alba.reader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UnknownFormatConversionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ComicBookZip {

    public static final String CBZ = ".cbz";

    private ComicBookZip() {

    }

    public static ComicBook load(String fileName) throws IOException {
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        if (!ext.equals(CBZ)) {
            throw new UnknownFormatConversionException("Can't format file, " + fileName + ", as a " + CBZ + " file.");
        }
        File file = new File(fileName);
        return load(file);
    }

    public static ComicBook load(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        System.out.println("beginning to unzip file");
        List<BufferedImage> images = unzip(file);
        System.out.println("unzipped file");
        ComicPage[] pages = new ComicPage[images.size()];
        System.out.println("made array the size of the amount of images which is: " + images.size());
        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
            System.out.println("Page: " + i + "added");
        }
        return new ComicBook(file.getName(), pages);
    }

    private static List<BufferedImage> unzip(File file) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // Only process image files
                if (!entry.isDirectory() && entry.getName().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                    try (InputStream is = zip.getInputStream(entry)) {
                        BufferedImage image = ImageIO.read(is);
                        if (image != null) {
                            // Optionally resize the image to reduce memory usage
                            BufferedImage resizedImage = EditImage.resizeImage(image, 800, 800); // Resize to max width/height of 800px
                            images.add(resizedImage);
                        } else {
                            System.err.println("Failed to read image: " + entry.getName());
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading entry: " + entry.getName());
                    }
                }
            }
        }
        return images;
    }

    public static void fileSize(File file) {
        //checking if the file exists or the file specified is of the type file
        if ((file.exists()) && (file.isFile()))
        {
            System.out.println("The File Size in KiloBytes : "+file.length() / 1024+"kb");
            System.out.println("The File Size in MegaBytes : "+file.length() / (1024 * 1024)+"mb");
        }
    }
}
