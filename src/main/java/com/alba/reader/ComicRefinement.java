package com.alba.reader;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class ComicRefinement {

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

    public static List<Comic> searchComics(List<Comic> comics, String query) {
        return comics.stream().filter(comic -> comic.title.toLowerCase().contains(query.toLowerCase())).toList();
    }
}
