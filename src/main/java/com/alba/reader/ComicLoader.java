package com.alba.reader;

import javax.swing.*;
import java.io.File;

public class ComicLoader {
    private final File file;
    private ComicBook comicBook;
    private final JProgressBar progressBar;
    private SwingWorker<ComicBook, Integer> worker;

    public ComicLoader(File file, JProgressBar progressBar) {
        this.file = file;
        this.progressBar = progressBar;
    }

    public void loadComicInBackground() {
        worker = new SwingWorker<>() {
            @Override
            protected ComicBook doInBackground() throws Exception {
                if (FileTypeDetector.isNhl(file)) {
                    System.out.println("nhl");
                    comicBook = ComicBookNhl.load(file);
                    for (int i = 0; i < comicBook.getPageCount(); i++) {
                        publish(i + 1); // Publish progress
                    }
                    return comicBook;
                } else if (FileTypeDetector.isRar(file)) {
                    System.out.println("rar");
                    comicBook = ComicBookRar.load(file);
                    for (int i = 0; i < comicBook.getPageCount(); i++) {
                        publish(i + 1); // Publish progress
                    }
                    return comicBook;
                } else if (FileTypeDetector.isZip(file)) {
                    System.out.println("zip");
                    comicBook = ComicBookZip.load(file);
                    for (int i = 0; i < comicBook.getPageCount(); i++) {
                        publish(i + 1); // Publish progress
                    }
                    return comicBook;
                } else {
                    throw new IllegalArgumentException("Unsupported file format");
                }
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                // Update progress bar based on published progress
                int progress = chunks.getLast(); // Get the last published progress
                progressBar.setValue(progress);
            }

            @Override
            protected void done() {
                progressBar.setValue(0); // Reset progress bar
            }
        };

        progressBar.setIndeterminate(true); // Show indeterminate progress while loading
        worker.execute();
    }

    public ComicBook getComicBook() {
        return comicBook;
    }

    public SwingWorker<ComicBook, Integer> getWorker() {
        return worker;
    }
}
