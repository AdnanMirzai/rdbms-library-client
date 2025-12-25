package se.kth.adnolle.rdbmslibraryclient.model;

import java.sql.Date;
import java.util.Collections;
import java.util.List;

/**
 * Representation of a Book.
 * @author adnolle@kth.se
 */
public class Book {

    private final int bookId;
    private final String isbn;
    private final String title;
    private final Date published;
    private final String storyLine;
    private final Integer rating;
    private final List<Author> authors;
    private final List<Genre> genres;

    public Book(int bookId, String isbn, String title, Date published,
                String storyLine, Integer rating, List<Author> authors, List<Genre> genres) throws IllegalArgumentException {

        if(!isbn.matches("\\d{13}")) throw new IllegalArgumentException();
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.storyLine = storyLine;
        this.rating = rating;
        this.authors = authors;
        this.genres = genres;
    }

    // For mock implementation, ID will be assigned by DB.
    public Book(String isbn, String title, Date published, String storyLine, Integer rating, List<Author> authors, List<Genre> genres) {
        this(-1, isbn, title, published, storyLine, rating, authors, genres);
    }

    // getters
    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public Date getPublished() { return published; }
    public String getStoryLine() { return storyLine; }
    public Integer getRating() { return rating; }
    public List<Author> getAuthors() { return Collections.unmodifiableList(authors); }
    public List<Genre> getGenres() { return Collections.unmodifiableList(genres); }

    @Override
    public String toString() {
        StringBuilder build;
        build = new StringBuilder(bookId + ", " + title + ", " + isbn + ", " + published.toString()
                + ", " + storyLine + ", " + "\nFörfattare: ");
        for (Author author : authors) {
            build.append(author.getName()).append(", ");
        }
        build.append("\nGenres: ");
        for (Genre genre : genres) {
            build.append(genre.getGenre()).append(", ");
        }
        return build.toString();
    }
}