package com.alba.reader;

import java.io.File;

public class FileTools {

    public static int getFileSizeMB(File file) {
        //checking if the file exists or the file specified is of the type file
        if ((file.exists()) && (file.isFile())) {
            return (int) (file.length() / (1024 * 1024));
        }
        return 0;
    }
}
