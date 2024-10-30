package com.alba.reader;

import java.awt.image.BufferedImage;

public record ComicPage(BufferedImage image) {

    /**
     * @return the width of the page as an integer
     */
    public int getWidth() {
        if (this.image == null) {
            return 0;
        }
        return this.image.getWidth();
    }

    /**
     * @return the height of the page as an integer
     */
    public int getHeight() {
        if (this.image == null) {
            return 0;
        }
        return this.image.getHeight();
    }

}
