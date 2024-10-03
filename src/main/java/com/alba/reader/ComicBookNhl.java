package com.alba.reader;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ComicBookNhl {

    public static final String NHL = ".nhlcomic";

    public static ComicBook load(String fileName) throws IOException {
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        if (!ext.equals(NHL)) {
            throw new UnknownFormatConversionException("Can't format file, " + fileName + ", as a " + NHL + " file.");
        }
        File file = new File(fileName);
        return load(file);
    }

    public static ComicBook load(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        return new ComicBook(file.getName(),getPagesFromGifInZip(file));
    }

    public static ComicPage[] getPagesFromGifInZip(File file) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            ComicPage[] pagesTemp = new ComicPage[0];
            JSONObject metadata = new JSONObject();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                // Only process GIF files
                if (!entry.isDirectory() && entry.getName().matches(".*\\.(gif)$")) {
                    try (InputStream inputStream = zip.getInputStream(entry);
                         ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {

                        // Get the ImageReader for GIF
                        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
                        if (readers.hasNext()) {
                            ImageReader reader = readers.next();

                            // Set the input for the reader
                            reader.setInput(imageInputStream);

                            int count = reader.getNumImages(true);
                            ComicPage[] pages = new ComicPage[count];
                            for (int index = 0; index < count; index++) {
                                BufferedImage image = reader.read(index);
                                pages[count - index - 1] = new ComicPage(image); // reverse page order
                            }
                            pagesTemp = pages;

                        }
                    } catch (IOException ex) {
                        // Handle exceptions as needed
                    }
                }

                if (!entry.isDirectory() && entry.getName().matches(".*\\.(json)$")) {
                    InputStream inputStream = zip.getInputStream(entry);
                    MetadataManager metadataManager = new MetadataManager(inputStream);
                    metadata = metadataManager.getMetadata();

                }
            }
            ComicListManager comicListManager = new ComicListManager();
            comicListManager.updateJSON(file.getName(), metadata);

            return pagesTemp;
        }
       // return new ComicPage[0];
    }



}
