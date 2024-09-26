package com.alba.reader;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class FileTypeDetector {

    private FileTypeDetector() {

    }

    public static boolean isZip(String fileName) {
        return isZip(new File(fileName));
    }

    public static boolean isZip(File file) {
        ZipFile zip = null;
        try {
            zip = new ZipFile(file);
            return zip.entries().hasMoreElements();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zip != null) {
                    zip.close();
                }
            } catch (IOException ignored) {

            }
        }
    }

    public static boolean isRar(String fileName) {
        return isRar(new File(fileName));
    }

    public static boolean isRar(File file) {
        Archive archive = null;
        try {
            archive = new Archive(file);
            return !archive.getFileHeaders().isEmpty();
        } catch (IOException | RarException e) {
            return false;
        } finally {
            try {
                if (archive != null) {
                    archive.close();
                }
            } catch (IOException ignored) {

            }
        }
    }
}
