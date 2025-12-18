/*/ package se.kth.adnolle.rdbmslibraryclient.model;

//vet att vi får duplicerade resultat på böcker i alla sökfunktioner, men eftersom detta bara är för att
// implementera view och controller så struntar vi att fixa, bara för test! Vi ska använda riktig databas sen

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import se.kth.adnolle.rdbmslibraryclient.model.exceptions.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A mock implementation of the IBooksDB interface to demonstrate how to
 * use it together with the user interface.
 * <p>
 * Your implementation must access a real database.
 * @author anderslm@kth.se
 */
/*/
public class IBooksDbMockImpl implements IBooksDb {

    private final List<Book> books; // the mock "database"
    private final List<Author> authors;
    private final List<Genre> genres;

    @Override
    public boolean connect(String database) throws ConnectionException {
        // mock implementation
        return true;
    }

    @Override
    public void disconnect() throws ConnectionException {
        // mock implementation
    }

    @Override
    public List<Book> findBooksByTitle(String title) throws SelectException {
        List<Book> result = new ArrayList<>();
        title = title.trim().toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(title)) {
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByIsbn(String isbn) throws SelectException {
        List<Book> result = new ArrayList<>();
        // add check for valid isbn ...
        isbn = isbn.trim().toLowerCase();
        for (Book book : books) {
            if (book.getIsbn().toLowerCase().equals(isbn)) { // exact match
                result.add(book);
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByAuthorName(String name) throws SelectException {
        List<Book> result = new ArrayList<>();
        name = name.trim().toLowerCase();
        for(Book book : books) {
            for(Author author : book.getAuthors()) {
                if(author.getName().trim().toLowerCase().contains(name)) {
                    result.add(book);
                }
            }
        }
        return result;
    }

    @Override
    public List<Book> findBooksByRating(String rating) throws SelectException {
        List<Book> result = new ArrayList<>();

        if (rating == null) return result;

        Integer ratingValue = tryParseInt(rating.trim());
        if (ratingValue == null) return result;

        for (Book book : books) {
            Integer r = book.getRating();
            if (r != null && r.equals(ratingValue)) {
                result.add(book);
            }
        }
        return result;
    }

    private Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Book> findBooksByGenre(String genre) throws SelectException {
        List<Book> result = new ArrayList<>();
        for(Book book : books) {
            for(Genre g : book.getGenres()) {
                if (g.getGenre().contains(genre)) {
                    result.add(book);
                }
            }
        }
        try{
            Thread.sleep(10000);
        } catch (Exception _) {}
        return result;
    }

    @Override
    public void addBook(Book book, List<Author> authors, List<Genre> genres) throws InsertException { //ej perfekt, bara mock
        int Id = books.size()+1;
        Book newBook = new Book(Id, book.getIsbn(), book.getTitle(), book.getPublished(), book.getStoryLine(),
                book.getRating(), book.getAuthors(), book.getGenres());
        //books.add(newBook); juste asList är immutable, så detta går ej, men vi låtsas
        try{
            Thread.sleep(10000);
        } catch (Exception _) {}
        System.out.println("Mock test: Added book " + newBook.getTitle());
    }

    @Override
    public void rateBook(int bookId, int rating) throws UpdateException {
        try{
            Thread.sleep(10000);
        } catch(Exception e){}
        System.out.println("Mock: Book to rate: " + bookId + " rating: " + rating);
    }

    @Override
    public List<Author> getAllAuthors() throws SelectException {
        return new ArrayList<>(authors);
    }

    @Override
    public List<Genre> getAllGenres() throws SelectException {
        return new ArrayList<>(genres);
    }

    @Override
    public boolean isConnected() throws ConnectionException {
        return false;
    }

    public IBooksDbMockImpl() {
        authors = Arrays.asList(
                new Author(1, "Catherine Ricardo", Date.valueOf("1999-11-11")),
                new Author(2, "Susan Ullman", Date.valueOf("1999-11-11")),
                new Author(3, "Kazuo Ishiguro", Date.valueOf("1999-11-11")),
                new Author(4, "Margaret Atwood", Date.valueOf("1999-11-11")),
                new Author(5, "Douglas Stuart", Date.valueOf("1999-11-11")),
                new Author(6, "Douglas Coupland", Date.valueOf("1999-11-11"))
        );

        genres = Arrays.asList(
                new Genre(1, "Database"),
                new Genre(2, "Fiction"),
                new Genre(3, "Literary Fiction"),
                new Genre(4, "Dystopian"),
                new Genre(5, "Historical Fiction"),
                new Genre(6, "Contemporary")
        );

        books = Arrays.asList(
                new Book(1, "1234567890123", "Databases Illuminated",
                        Date.valueOf("2018-01-01"),
                        "A comprehensive guide to database systems.", 5,
                        Arrays.asList(authors.get(0), authors.get(1)), // Ricardo & Ullman
                        Arrays.asList(genres.get(0))), // Database

                new Book(2, "2345678901234", "Dark Databases",
                        Date.valueOf("1990-01-01"),
                        "A thriller about database security.", 1,
                        Arrays.asList(authors.get(0)), // Ricardo
                        Arrays.asList(genres.get(0), genres.get(1))), // Database, Fiction

                new Book(3, "4567890123456", "The buried giant",
                        Date.valueOf("2015-03-01"),
                        "A fantasy novel set in post-Arthurian Britain.", null,
                        Arrays.asList(authors.get(2)), // Ishiguro
                        Arrays.asList(genres.get(1), genres.get(2))), // Fiction, Literary Fiction

                new Book(4, "5678901234567", "Never let me go",
                        Date.valueOf("2005-01-01"),
                        "A dystopian science fiction novel.", null,
                        Arrays.asList(authors.get(2)), // Ishiguro
                        Arrays.asList(genres.get(2), genres.get(3))), // Literary Fiction, Dystopian

                new Book(5, "6789012345678", "The remains of the day",
                        Date.valueOf("1989-05-01"),
                        "A story of an English butler in post-war England.", null,
                        Arrays.asList(authors.get(2)), // Ishiguro
                        Arrays.asList(genres.get(2), genres.get(4))), // Literary Fiction, Historical Fiction

                new Book(6, "2345678900000", "Alias Grace",
                        Date.valueOf("1996-09-01"),
                        "Based on true events about a convicted murderess.", 2,
                        Arrays.asList(authors.get(3)), // Atwood
                        Arrays.asList(genres.get(2), genres.get(4))), // Literary Fiction, Historical Fiction

                new Book(7, "3456789111111", "The handmaids tale",
                        Date.valueOf("1985-01-01"),
                        "A dystopian novel about a totalitarian society.", 3,
                        Arrays.asList(authors.get(3)), // Atwood
                        Arrays.asList(genres.get(3), genres.get(1))), // Dystopian, Fiction

                new Book(8, "3456789012222", "Shuggie Bain",
                        Date.valueOf("2020-01-01"),
                        "A story of growing up in 1980s Glasgow.", 4,
                        Arrays.asList(authors.get(4)), // Stuart
                        Arrays.asList(genres.get(2), genres.get(5))), // Literary Fiction, Contemporary

                new Book(9, "3456789123333", "Microserfs",
                        Date.valueOf("1995-01-01"),
                        "Life at Microsoft in the early 1990s.", 5,
                        Arrays.asList(authors.get(5)), // Coupland
                        Arrays.asList(genres.get(1), genres.get(5))) // Fiction, Contemporary
        );
    }

}
/*/
