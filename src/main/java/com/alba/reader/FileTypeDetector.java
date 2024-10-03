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

    public static boolean isZip(String fileName) {return isZip(new File(fileName));
    }

    public static boolean isZip(File file) {
        try (ZipFile zip = new ZipFile(file)) {
            return zip.entries().hasMoreElements();
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isNhl(String fileName) {
        return isNhl(new File(fileName));
    }

    public static boolean isNhl(File file){
        if (isZip(file)) {
            try (ZipFile zip = new ZipFile(file)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().matches(".*\\.(gif)$"))
                        return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isRar(String fileName) {return isRar(new File(fileName));}

    public static boolean isRar(File file) {
        try (Archive archive = new Archive(file)) {
            return !archive.getFileHeaders().isEmpty();
        } catch (IOException | RarException e) {
            return false;
        }
    }
}
