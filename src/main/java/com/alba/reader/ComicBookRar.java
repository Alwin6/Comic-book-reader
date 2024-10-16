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
    }
}
