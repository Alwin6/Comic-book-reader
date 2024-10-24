package com.alba.reader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

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
        List<BufferedImage> images = extractImages(file);

        ComicPage[] pages = new ComicPage[images.size()];

        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
        }
        return new ComicBook(file.getName(), pages);
    }

    private static List<BufferedImage> extractImages(File file) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        JSONObject metadata = new JSONObject();
        try (Archive archive = new Archive(file)) {
            FileHeader fileHeader = archive.nextFileHeader();
            while (fileHeader != null) {
                // Only process image files
                if (!fileHeader.isDirectory() && fileHeader.getFileName().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                    try (InputStream is = archive.getInputStream(fileHeader);
                         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                        byte[] buffer = new byte[16384];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                        if (image != null) {
                            images.add(image);

                        } else {
                            System.err.println("Failed to read image: " + fileHeader.getFileName());
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading entry: " + fileHeader.getFileName());
                    }
                }

                if (!fileHeader.isDirectory() && fileHeader.getFileName().matches(".*\\.(xml)$")) {
                    InputStream inputStream = archive.getInputStream(fileHeader);
                    MetadataManager metadataManager = new MetadataManager(inputStream);
                    metadata = metadataManager.XMLtoMetadata();
                }

                fileHeader = archive.nextFileHeader();
            }
        } catch (RarException e) {
            throw new IOException("Error reading RAR file", e);
        }
        ComicListManager comicListManager = new ComicListManager();
        comicListManager.updateJSON(file.getName(), metadata, file.getAbsolutePath());

        return images; // Return the list of images
    }

    public static List<FileHeader> getMatchingEntries(File file, List<String> fileTypes) throws IOException {
        List<FileHeader> entries = new ArrayList<>();

        System.out.println("Starting to read RAR file: " + file.getName());

        try (Archive archive = new Archive(file)) {
            long rarOpenStartTime = System.currentTimeMillis();
            FileHeader fileHeader = archive.nextFileHeader();

            while (fileHeader != null) {
                if (!fileHeader.isDirectory()) {
                    String entryName = fileHeader.getFileName();

                    // Check if entry name matches any of the provided file types
                    if (fileTypes.stream().anyMatch(entryName::endsWith)) {
                        entries.add(fileHeader);
                    }
                }
                fileHeader = archive.nextFileHeader();
            }
            long rarOpenEndTime = System.currentTimeMillis();
            System.out.println("Time taken to open RAR and read entries: " + (rarOpenEndTime - rarOpenStartTime) + " ms");
        } catch (RarException e) {
            throw new IOException("Error reading RAR file", e);
        }

        return entries; // Return the list of matching entries
    }

    public static BufferedImage extractFirstImage(File file) throws IOException {
        BufferedImage firstImage = null;
        System.out.println("Starting to read RAR file: " + file.getName());
        try (Archive archive = new Archive(file)) {
            long rarOpenStartTime = System.currentTimeMillis();
            FileHeader fileHeader = archive.nextFileHeader();
            while (fileHeader != null) {
                // Only process image files
                if (!fileHeader.isDirectory() && fileHeader.getFileName().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                    try (InputStream is = archive.getInputStream(fileHeader);
                         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                        byte[] buffer = new byte[16384];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        firstImage = ImageIO.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                        if (firstImage != null) {
                            break; // Exit after processing the first image
                        } else {
                            System.err.println("Failed to read image: " + fileHeader.getFileName());
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading entry: " + fileHeader.getFileName());
                    }
                }
                fileHeader = archive.nextFileHeader();
            }
            long rarOpenEndTime = System.currentTimeMillis();
            System.out.println("Time taken to open RAR and read image: " + (rarOpenEndTime - rarOpenStartTime) + " ms");
        } catch (RarException e) {
            throw new IOException("Error reading RAR file", e);
        }

        return firstImage; // Return the first image found, or null if none
    }
}
