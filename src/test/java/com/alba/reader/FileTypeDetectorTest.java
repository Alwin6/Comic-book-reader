package com.alba.reader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileTypeDetectorTest {

    private final String zipFilePath = "src/test/resources/Atomic_Comic__Fudge_ca._1947___UK_.cbz";
    private final String nhlFilePath = "src/test/resources/pepper&carrot_1.nhlcomic";
    private final String rarFilePath = "src/test/resources/THEGREATEST.cbr";

    @Test
    public void testIsZip() {
        assertTrue(FileTypeDetector.isZip(zipFilePath), "The file should be recognized as a ZIP file.");
        assertFalse(FileTypeDetector.isZip(nhlFilePath), "The NHL comic file should not be recognized as a ZIP file.");
        assertFalse(FileTypeDetector.isZip(rarFilePath), "A RAR file should not be recognized as a ZIP file.");
    }

    @Test
    public void testIsNhl() {
        assertTrue(FileTypeDetector.isNhl(nhlFilePath), "The file should be recognized as an NHL comic.");
        assertFalse(FileTypeDetector.isNhl(zipFilePath), "The ZIP file should not be recognized as an NHL comic.");
        assertFalse(FileTypeDetector.isNhl(rarFilePath), "A RAR file should not be recognized as an NHL comic.");
    }

    @Test
    public void testIsRar() {
        assertTrue(FileTypeDetector.isRar(rarFilePath), "The file should be recognized as a RAR file.");
        assertFalse(FileTypeDetector.isRar(nhlFilePath), "The NHL comic file should not be recognized as a ZIP file.");
        assertFalse(FileTypeDetector.isRar(zipFilePath), "A ZIP file should not be recognized as a RAR file.");
    }

    @Test
    public void testInvalidFilePaths() {
        assertFalse(FileTypeDetector.isZip("invalid/path/to/file.zip"), "An invalid ZIP file path should return false.");
        assertFalse(FileTypeDetector.isNhl("invalid/path/to/file.zip"), "An invalid NHL file path should return false.");
        assertFalse(FileTypeDetector.isRar("invalid/path/to/file.rar"), "An invalid RAR file path should return false.");
    }
}
