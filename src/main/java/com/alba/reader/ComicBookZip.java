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

    /**
     * @param fileName
     * @return a ComicBook
     * @throws IOException
     */
    public static ComicBook load(String fileName) throws IOException {
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        if (!ext.equals(CBZ)) {
            throw new UnknownFormatConversionException("Can't format file, " + fileName + ", as a " + CBZ + " file.");
        }
        File file = new File(fileName);
        return load(file);
    }

    /**
     * @param file
     * @return a ComicBook
     * @throws IOException
     */
    public static ComicBook load(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        // Unzip and get image entries
        List<ZipEntry> imageEntries = getMatchingEntries(file, Arrays.asList("jpg", "jpeg", "png", "gif"));
        List<BufferedImage> images = processImageEntries(file, imageEntries);

        // Process metadata entries
        processMetadataEntries(file);

        ComicPage[] pages = new ComicPage[images.size()];
        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
        }
        return new ComicBook(file.getName(), pages);
    }

    /**
     * Find entries in the archive that match any of the given strings
     * @param file
     * @param fileTypes
     * @return a list of entries
     * @throws IOException
     */
    public static List<ZipEntry> getMatchingEntries(File file, List<String> fileTypes) throws IOException {
        List<ZipEntry> entries = new ArrayList<>();

        try (ZipFile zip = new ZipFile(file)) {
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
        }
        return entries;
    }

    /**
     * Process multiple images
     * @param file
     * @param imageEntries
     * @return a list of images
     * @throws IOException
     */
    private static List<BufferedImage> processImageEntries(File file, List<ZipEntry> imageEntries) throws IOException {
        List<BufferedImage> images;

        try (ZipFile zip = new ZipFile(file)) {
            images = imageEntries.parallelStream()
                    .map(entry -> processImageEntry(zip, entry))
                    .filter(Objects::nonNull)  // Filter out any null images
                    .collect(Collectors.toList());
        }

        return images;
    }

    /**
     * Process a single entry
     * @param zip
     * @param entry
     * @return an image
     */
    private static BufferedImage processImageEntry(ZipFile zip, ZipEntry entry) {
        try (InputStream is = zip.getInputStream(entry)) {
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                return image; // Return the image
            }
        } catch (IOException ignored) {}
        return null; // Return null for any failed image processing
    }

    /**
     * Extract metadata from the zip and save it in the comic list
     * @param file
     * @throws IOException
     */
    private static void processMetadataEntries(File file) throws IOException {
        // Get XML entries using unzip method
        List<ZipEntry> xmlEntries = getMatchingEntries(file, Collections.singletonList("xml"));
        ConcurrentHashMap<String, Object> metadata = new ConcurrentHashMap<>();

        try (ZipFile zip = new ZipFile(file)) {
            for (ZipEntry entry : xmlEntries) {
                processMetadataEntry(zip, entry, metadata);
            }
            updateComicList(file.getName(), new JSONObject(metadata), file.getAbsolutePath());
        }
    }

    /**
     * Process a single metadata entry
     * @param zip
     * @param entry
     * @param metadata
     */
    private static void processMetadataEntry(ZipFile zip, ZipEntry entry, ConcurrentHashMap<String, Object> metadata) {
        try (InputStream inputStream = zip.getInputStream(entry)) {
            MetadataManager metadataManager = new MetadataManager(inputStream);
            JSONObject entryMetadata = metadataManager.XMLtoMetadata();

            // Merge entryMetadata into metadata
            for (String key : entryMetadata.keySet()) {
                metadata.put(key, entryMetadata.get(key));
            }
        } catch (IOException ignored) {}
    }

    /**
     * Update the comic list with the relevant information obtained through unzipping
     * @param fileName
     * @param metadata
     * @param path
     * @throws IOException
     */
    private static void updateComicList(String fileName, JSONObject metadata, String path) throws IOException {
        ComicListManager comicListManager = new ComicListManager();
        comicListManager.updateJSON(fileName, metadata, path);
    }
}
