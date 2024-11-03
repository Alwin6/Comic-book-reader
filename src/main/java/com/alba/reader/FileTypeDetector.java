package com.alba.reader;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileTypeDetector {

    private FileTypeDetector() {

    }

    /**
     * @param filePath
     * @return if the file is a zip file
     */
    public static boolean isZip(String filePath) {
        return isZip(new File(filePath));
    }

    /**
     * @param file
     * @return if the file is a zip file
     */
    public static boolean isZip(File file) {
        try (ZipFile zip = new ZipFile(file)) {
            // Check for the presence of .gif files to determine if it's an NHL comic
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().matches(".*\\.gif$")) {
                    return false; // It's an NHL comic, so return false
                }
            }
            return zip.entries().hasMoreElements(); // Return true if it's a ZIP file with no NHL comic content
        } catch (IOException e) {
            return false; // Not a valid ZIP file
        }
    }

    /**
     * @param filePath
     * @return if the file is an nhlcomic file
     */
    public static boolean isNhl(String filePath) {
        return isNhl(new File(filePath));
    }

    /**
     * @param file
     * @return if the file is an nhlcomic file
     */
    public static boolean isNhl(File file) {
        try (ZipFile zip = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().matches(".*\\.gif$")) {
                    return true; // Found a GIF, confirming it's an NHL comic
                }
            }
        } catch (IOException e) {
            return false; // If the file isn't a valid ZIP, return false
        }
        return false; // No GIF found
    }

    /**
     * @param filePath
     * @return if the file is a rar file
     */
    public static boolean isRar(String filePath) {
        return isRar(new File(filePath));}

    /**
     * @param file
     * @return if the file is a rar file
     */
    public static boolean isRar(File file) {
        try (Archive archive = new Archive(file)) {
            return !archive.getFileHeaders().isEmpty();
        } catch (IOException | RarException e) {
            return false;
        }
    }
}
