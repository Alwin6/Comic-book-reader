package com.alba.reader;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        List<BufferedImage> images = unzip(file);

        ComicPage[] pages = new ComicPage[images.size()];

        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
        }
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
        long startTime = System.currentTimeMillis();
        List<BufferedImage> images;
        ConcurrentHashMap<String, Object> metadata = new ConcurrentHashMap<>();

        System.out.println("Starting to unzip file: " + file.getName());

        try (ZipFile zip = new ZipFile(file)) {
            long zipOpenStartTime = System.currentTimeMillis();
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
            long zipOpenEndTime = System.currentTimeMillis();
            System.out.println("Time taken to open zip and read entries: " + (zipOpenEndTime - zipOpenStartTime) + " ms");

            // Process images sequentially
            System.out.println("Processing images...");
            long imageProcessingStartTime = System.currentTimeMillis();
            images = imageEntries.parallelStream()
                    .map(entry -> processImageEntry(zip, entry))
                    .filter(Objects::nonNull)  // Filter out any null images
                    .collect(Collectors.toList());
            long imageProcessingEndTime = System.currentTimeMillis();
            System.out.println("Time taken to process images: " + (imageProcessingEndTime - imageProcessingStartTime) + " ms");
            System.out.println("Finished processing images. Total images processed: " + images.size());
        }

        updateComicList(file.getName(), new JSONObject(metadata));
        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for unzipping: " + (endTime - startTime) + " ms");
        return images;
    }

    private static BufferedImage processImageEntry(ZipFile zip, ZipEntry entry) {
        try (InputStream is = zip.getInputStream(entry)) {
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                System.out.println("Successfully read image: " + entry.getName());
                return image; // Return the original image without resizing
            } else {
                System.err.println("Failed to read image (null): " + entry.getName());
            }
        } catch (IOException e) {
            System.err.println("Error reading image entry: " + entry.getName() + " - " + e.getMessage());
        }
        return null; // Return null for any failed image processing
    }

    private static void processMetadataEntry(ZipFile zip, ZipEntry entry, ConcurrentHashMap<String, Object> metadata) {
        try (InputStream inputStream = zip.getInputStream(entry)) {
            MetadataManager metadataManager = new MetadataManager(inputStream);
            JSONObject entryMetadata = metadataManager.XMLtoMetadata();

            // Merge entryMetadata into metadata
            for (String key : entryMetadata.keySet()) {
                metadata.put(key, entryMetadata.get(key));
                System.out.println("Added metadata entry: " + key);
            }
        } catch (IOException e) {
            System.err.println("Error reading metadata entry: " + entry.getName() + " - " + e.getMessage());
        }
    }

    private static void updateComicList(String fileName, JSONObject metadata) throws IOException {
        ComicListManager comicListManager = new ComicListManager();
        comicListManager.updateJSON(fileName, metadata);
        System.out.println("Updated comic list JSON for: " + fileName);
    }
}
