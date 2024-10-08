package com.alba.reader;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ComicBookZip {

    private static final String CBZ = ".cbz";

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

        long startTime = System.nanoTime();
        System.out.println("Beginning to unzip file");
        List<BufferedImage> images = unzip(file);
        long unzipTime = System.nanoTime() - startTime;
        System.out.println("Unzipped file in " + (unzipTime / 1_000_000) + " ms");

        ComicPage[] pages = new ComicPage[images.size()];
        System.out.println("Made array the size of the amount of images which is: " + images.size());

        startTime = System.nanoTime();
        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
            System.out.println("Page: " + i + " added");
        }
        long loadingTime = System.nanoTime() - startTime;
        System.out.println("Loaded images in " + (loadingTime / 1_000_000) + " ms");

        return new ComicBook(file.getName(), pages);
    }


    /*private static List<BufferedImage> unzip(File file) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        JSONObject metadata;
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            metadata = new JSONObject();
            int fileSize = FileTools.getFileSizeMB(file);
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // Only process image files
                if (!entry.isDirectory() && entry.getName().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                    try (InputStream is = zip.getInputStream(entry)) {
                        BufferedImage image = ImageIO.read(is);
                        if (image != null) {
                            // Resize image if the file size is above 800MB
                            if (fileSize > 800) {
                                BufferedImage resizedImage = ImageTools.resizeImage(image, 800, 800); // Resize to max width/height of 800px
                                images.add(resizedImage);
                            } else {
                                images.add(image);
                            }
                        } else {
                            System.err.println("Failed to read image: " + entry.getName());
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading entry: " + entry.getName());
                    }
                }

                if (!entry.isDirectory() && entry.getName().matches(".*\\.(xml)$")) {
                    InputStream inputStream = zip.getInputStream(entry);
                    MetadataManager metadataManager = new MetadataManager(inputStream);
                    metadata = metadataManager.XMLtoMetadata();
                }
            }
        }
        ComicListManager comicListManager = new ComicListManager();
        comicListManager.updateJSON(file.getName(), metadata);

        return images;
    }*/

    private static List<BufferedImage> unzip(File file) throws IOException {
        List<BufferedImage> images;
        JSONObject metadata = new JSONObject();

        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            List<ZipEntry> imageEntries = new ArrayList<>();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (!entry.isDirectory()) {
                    if (entryName.matches(".*\\.(jpg|jpeg|png|gif)$")) {
                        imageEntries.add(entry);
                    } else if (entryName.endsWith(".xml")) {
                        processMetadataEntry(zip, entry, metadata);
                    }
                }
            }

            // Process images in parallel
            images = imageEntries.parallelStream()
                    .map(entry -> processImageEntry(zip, entry))
                    .filter(Objects::nonNull)  // Filter out any null images
                    .collect(Collectors.toList());
        }

        updateComicList(file.getName(), metadata);
        return images;
    }

    private static BufferedImage processImageEntry(ZipFile zip, ZipEntry entry) {
        try (InputStream is = zip.getInputStream(entry)) {
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                // Return the original image without resizing
                return image;
            } else {
                System.err.println("Failed to read image: " + entry.getName());
            }
        } catch (IOException e) {
            System.err.println("Error reading image entry: " + entry.getName());
        }
        return null; // Return null for any failed image processing
    }

    private static void processMetadataEntry(ZipFile zip, ZipEntry entry, JSONObject metadata) {
        try (InputStream inputStream = zip.getInputStream(entry)) {
            MetadataManager metadataManager = new MetadataManager(inputStream);
            JSONObject entryMetadata = metadataManager.XMLtoMetadata();

            // Merge entryMetadata into metadata
            for (String key : entryMetadata.keySet()) {
                metadata.put(key, entryMetadata.get(key));
            }
        } catch (IOException e) {
            System.err.println("Error reading metadata entry: " + entry.getName());
        }
    }

    private static void updateComicList(String fileName, JSONObject metadata) throws IOException {
        ComicListManager comicListManager = new ComicListManager();
        comicListManager.updateJSON(fileName, metadata);
    }
}
