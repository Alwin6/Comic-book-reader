package com.alba.reader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UnknownFormatConversionException;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;

public class ComicBookRar {

    public static final String CBR = ".cbr";

    private ComicBookRar() {
    }

    public static ComicBook load(String fileName) throws IOException {
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        if (!ext.equals(CBR)) {
            throw new UnknownFormatConversionException("Can't format file, " + fileName + ", as a " + CBR + " file.");
        }
        File file = new File(fileName);
        return load(file);
    }

    public static ComicBook load(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        System.out.println("Beginning to extract file");
        List<BufferedImage> images = extractImages(file);
        System.out.println("Extracted file");
        ComicPage[] pages = new ComicPage[images.size()];
        System.out.println("Created array of size: " + images.size());
        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
            System.out.println("Page: " + i + " added");
        }
        return new ComicBook(file.getName(), pages);
    }

    private static List<BufferedImage> extractImages(File file) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        try (Archive archive = new Archive(file)) {
            FileHeader fileHeader = archive.nextFileHeader();
            while (fileHeader != null) {
                // Only process image files
                if (!fileHeader.isDirectory() && isImageFile(fileHeader.getFileNameString())) {
                    try (InputStream is = archive.getInputStream(fileHeader);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                        byte[] buffer = new byte[16384];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                        if (image != null) {
                            // Optionally resize the image to reduce memory usage
                            BufferedImage resizedImage = EditImage.resizeImage(image, 800, 800); // Resize to max width/height of 800px
                            images.add(resizedImage);
                        } else {
                            System.err.println("Failed to read image: " + fileHeader.getFileNameString());
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading entry: " + fileHeader.getFileNameString());
                    }
                }
                fileHeader = archive.nextFileHeader();
            }
        } catch (RarException e) {
            throw new IOException("Error reading RAR file", e);
        }
        return images; // Return the list of images
    }

    private static boolean isImageFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".gif");
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
