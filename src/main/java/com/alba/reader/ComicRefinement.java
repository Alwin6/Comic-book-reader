package com.alba.reader;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ComicRefinement {

    /**
     * Sorts a list of comic by the desired attributes
     * @param comics
     * @param by a string to set the attribute, may be:<br>
     *           "Title": to sort by the title<br>
     *           "Last opened": to sort by the date on which the comic was last opened<br>
     *           "Pages": to sort by the amount of pages
     * @param order order accepts true to sort in ascending order, and false to sort in descending order
     * @return a list of comics
     */
    public static List<Comic> sortComics(List<Comic> comics, String by, Boolean order) {
        Comparator<Comic> comparator = Comparator.comparing(comic -> comic.title, String.CASE_INSENSITIVE_ORDER);

        switch (by) {
            case "Title":
                comparator = Comparator.comparing(comic -> comic.title, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Last opened":
                comparator = Comparator.comparing(comic -> comic.lastOpened);
                break;
            case "Pages":
                comparator = Comparator.comparingInt(comic -> comic.totalPages);
                break;
        }
        if (!order) {
            comparator = comparator.reversed();
        }
        comics.sort(comparator);

        return comics;
    }

    /**
     * Filter comics by a desired attribure
     * @param comics
     * @param by a string to specify by what attribute to filter, may be:<br>
     *           "Read": if the comic has been read<br>
     *           "Not read" if the comic hasn't been read<br>
     *           "Reading": if the comic isn't on page 1 and isn't read<br>
     *           "Favorite": If the comic is a favorite<br>
     *           "Not favorite": If the comic is not a favorite
     * @return a list of comics
     */
    public static List<Comic> filterComics(List<Comic> comics, String by) {
        Predicate<Comic> predicate = comic -> true;
        switch (by) {
            case "Read":
                predicate = comic -> comic.read;
                break;
            case "Not read":
                predicate = comic -> !comic.read;
                break;
            case "Reading":
                predicate = comic -> comic.currentPage != 0 && !comic.read;
                break;
            case "Favorite":
                predicate = comic -> comic.favorite;
                break;
            case "Not favorite":
                predicate = comic -> !comic.favorite;
                break;

        }

        return comics.stream().filter(predicate).toList();
    }

    /**
     * Filter comics bu those that contain a string, not case sensitive
     * @param comics
     * @param query
     * @return a list of comics
     */
    public static List<Comic> searchComics(List<Comic> comics, String query) {
        return comics.stream().filter(comic -> comic.title.toLowerCase().contains(query.toLowerCase())).toList();
    }
}
