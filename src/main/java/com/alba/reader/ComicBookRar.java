package com.alba.reader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import org.json.JSONObject;
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
        List<BufferedImage> images = unzip(file);

        ComicPage[] pages = new ComicPage[images.size()];

        for (int i = 0; i < images.size(); i++) {
            pages[i] = new ComicPage(images.get(i));
        }
        return new ComicBook(file.getName(), pages);
    }

    // original
    /*private static List<BufferedImage> extractImages(File file) throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        JSONObject metadata = new JSONObject();
        try (Archive archive = new Archive(file)) {
            FileHeader fileHeader = archive.nextFileHeader();
            int fileSize = FileTools.getFileSizeMB(file);
            while (fileHeader != null) {
                // Only process image files
                if (!fileHeader.isDirectory() && fileHeader.getFileName().matches(".*\\.(jpg|jpeg|png|gif)$")) {
                    try (InputStream is = archive.getInputStream(fileHeader);
                         ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                        byte[] buffer = new byte[16384];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
                        if (image != null) {
                            // Resize image if the file size is above 800MB
                            if(fileSize > 800){
                                BufferedImage resizedImage = ImageTools.resizeImage(image, 800, 800); // Resize to max width/height of 800px
                                images.add(resizedImage);
                            }
                            else{
                                images.add(image);
                            }

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
        comicListManager.updateJSON(file.getName(), metadata);

        return images; // Return the list of images
    }*/

    // attempt changing
    /*private static List<BufferedImage> unzip(File file) throws IOException {
        long startTime = System.currentTimeMillis();
        List<BufferedImage> images;
        ConcurrentHashMap<String, Object> metadata = new ConcurrentHashMap<>();

        System.out.println("Starting to unzip file: " + file.getName());

        // Create an Archive object using the File
        try (Archive rarArchive = new Archive(file)) {
            List<FileHeader> imageEntries = new ArrayList<>();
            FileHeader header;

            while ((header = rarArchive.nextFileHeader()) != null) {
                String entryName = header.getFileName().trim();

                if (!header.isDirectory()) {
                    if (entryName.matches(".*\\.(jpg|jpeg|png|gif)$")) {
                        imageEntries.add(header);
                    } else if (entryName.endsWith(".xml")) {
                        processMetadataEntry(rarArchive, header, metadata);
                    }
                }
            }

            // Process images sequentially
            System.out.println("Processing images...");
            long imageProcessingStartTime = System.currentTimeMillis();
            images = imageEntries.parallelStream()
                    .map(entryHeader -> processImageEntry(rarArchive, entryHeader))
                    .filter(Objects::nonNull)  // Filter out any null images
                    .collect(Collectors.toList());
            long imageProcessingEndTime = System.currentTimeMillis();
            System.out.println("Time taken to process images: " + (imageProcessingEndTime - imageProcessingStartTime) + " ms");
            System.out.println("Finished processing images. Total images processed: " + images.size());
        } catch (IOException | RarException e) {
            System.err.println("Error opening RAR archive: " + e.getMessage());
            return Collections.emptyList(); // Return an empty list on error
        }

        updateComicList(file.getName(), new JSONObject(metadata));
        long endTime = System.currentTimeMillis();
        System.out.println("Total time taken for unzipping: " + (endTime - startTime) + " ms");
        return images;
    }

    private static BufferedImage processImageEntry(Archive rarArchive, FileHeader header) {
        try (InputStream is = rarArchive.getInputStream(header);
             BufferedInputStream bufferedStream = new BufferedInputStream(is)) {

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = bufferedStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }
            buffer.flush();

            byte[] imageData = buffer.toByteArray();
            if (imageData.length == 0) {
                System.err.println("Image data is empty: " + header.getFileNameString());
                return null;
            }

            // Create an InputStream from the byte array for ImageIO
            try (InputStream byteStream = new ByteArrayInputStream(imageData)) {
                BufferedImage image = ImageIO.read(byteStream);
                if (image != null) {
                    System.out.println("Successfully read image: " + header.getFileNameString());
                    return image; // Return the original image without resizing
                } else {
                    System.err.println("Failed to read image (null): " + header.getFileNameString());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading image entry: " + header.getFileNameString() +
                    " - " + e.getMessage());
        }
        return null; // Return null for any failed image processing
    }

    private static void processMetadataEntry(Archive rarArchive, FileHeader header, ConcurrentHashMap<String, Object> metadata) {
        try (InputStream inputStream = rarArchive.getInputStream(header)) {
            MetadataManager metadataManager = new MetadataManager(inputStream);
            JSONObject entryMetadata = metadataManager.XMLtoMetadata();

            // Merge entryMetadata into metadata
            for (String key : entryMetadata.keySet()) {
                metadata.put(key, entryMetadata.get(key));
                System.out.println("Added metadata entry: " + key);
            }
        } catch (IOException e) {
            System.err.println("Error reading metadata entry: " + header.getFileName() + " - " + e.getMessage());
        }
    }

    private static void updateComicList(String fileName, JSONObject metadata) throws IOException {
        ComicListManager comicListManager = new ComicListManager();
        comicListManager.updateJSON(fileName, metadata);
        System.out.println("Updated comic list JSON for: " + fileName);
    }*/

    private static List<BufferedImage> unzip(File file) throws IOException {
        return null;
    }
}
