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

        // Unzip and get image entries
        long startTime = System.currentTimeMillis();
        List<ZipEntry> imageEntries = unzip(file, Arrays.asList("jpg", "jpeg", "png", "gif"));
        List<BufferedImage> images = processImageEntries(file, imageEntries);
        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for unzipping: " + (endTime - startTime) + " ms");

        // Process metadata entries
        processMetadataEntries(file);

        ComicPage[] pages = new ComicPage[images.size()];
        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
        }
        return new ComicBook(file.getName(), pages);
    }

    public static List<ZipEntry> unzip(File file, List<String> fileTypes) throws IOException {
        List<ZipEntry> entries = new ArrayList<>();

        System.out.println("Starting to unzip file: " + file.getName());

        try (ZipFile zip = new ZipFile(file)) {
            long zipOpenStartTime = System.currentTimeMillis();
            Enumeration<? extends ZipEntry> zipEntries = zip.entries();

            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                String entryName = entry.getName();

                if (!entry.isDirectory()) {
                    // Check if entry name matches any of the provided file types
                    if (fileTypes.stream().anyMatch(entryName::endsWith)) {
                        entries.add(entry);
                    }
                }
            }
            long zipOpenEndTime = System.currentTimeMillis();
            System.out.println("Time taken to open zip and read entries: " + (zipOpenEndTime - zipOpenStartTime) + " ms");
        }
        return entries;
    }

    private static List<BufferedImage> processImageEntries(File file, List<ZipEntry> imageEntries) throws IOException {
        List<BufferedImage> images;

        try (ZipFile zip = new ZipFile(file)) {
            System.out.println("Processing images...");
            images = imageEntries.parallelStream()
                    .map(entry -> processImageEntry(zip, entry))
                    .filter(Objects::nonNull)  // Filter out any null images
                    .collect(Collectors.toList());
        }

        System.out.println("Finished processing images. Total images processed: " + images.size());
        return images;
    }

    private static BufferedImage processImageEntry(ZipFile zip, ZipEntry entry) {
        try (InputStream is = zip.getInputStream(entry)) {
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                System.out.println("Successfully read image: " + entry.getName());
                return image; // Return the image
            } else {
                System.err.println("Failed to read image (null): " + entry.getName());
            }
        } catch (IOException e) {
            System.err.println("Error reading image entry: " + entry.getName() + " - " + e.getMessage());
        }
        return null; // Return null for any failed image processing
    }

    private static void processMetadataEntries(File file) throws IOException {
        // Get XML entries using unzip method
        List<ZipEntry> xmlEntries = unzip(file, Collections.singletonList("xml"));
        ConcurrentHashMap<String, Object> metadata = new ConcurrentHashMap<>();

        try (ZipFile zip = new ZipFile(file)) {
            for (ZipEntry entry : xmlEntries) {
                processMetadataEntry(zip, entry, metadata);
            }
            updateComicList(file.getName(), new JSONObject(metadata), file.getAbsolutePath());
        }
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

    private static void updateComicList(String fileName, JSONObject metadata, String path) throws IOException {
        ComicListManager comicListManager = new ComicListManager();
        comicListManager.updateJSON(fileName, metadata, path);
        System.out.println("Updated comic list JSON for: " + fileName);
    }
}
