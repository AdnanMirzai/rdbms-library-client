package se.kth.adnolle.rdbmslibraryclient.model;

import java.sql.Date;

/**
 * Representation of an Author.
 * with auId and name attributes
 * @author adnolle@kth.se
 */
public class Author {
    private final int auId;
    private final String name;
    private final Date DOB;

    public Author(int auId, String name, Date DOB) {
        this.auId = auId;
        this.name = name;
        this.DOB = DOB;
    }

    public Author(String name, Date DOB) {
        this(-1, name, DOB);
    }

    public int getAuId() { return auId; }
    public String getName() { return name; }
    public Date getDOB() { return DOB; }

    @Override
    public String toString() { return "AuthorID: " + auId + "\nAuthor Name: " + name + "\nDate of birth: " + DOB; }
}