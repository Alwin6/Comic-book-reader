package com.alba.reader;

import java.awt.image.BufferedImage;

public record ComicPage(BufferedImage image) {

    public int getWidth() {
        if (this.image == null) {
            return 0;
        }
        return this.image.getWidth();
    }

    public int getHeight() {
        if (this.image == null) {
            return 0;
        }
        return this.image.getHeight();
    }

}
