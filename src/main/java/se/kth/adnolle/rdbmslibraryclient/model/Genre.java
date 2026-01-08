package se.kth.adnolle.rdbmslibraryclient.model;

/**
 * Representation of a Genre.
 *
 * @author adnolle@kth.se
 */
public class Genre {
    private final int genreId;
    private final String genre;

    public Genre(int genreId, String genre) {
        this.genreId = genreId;
        this.genre = genre;
    }

    public Genre(String genre) {
        this(-1, genre);
    }

    public int getGenreId() {
        return genreId;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return genre;
    }
}
