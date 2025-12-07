package se.kth.adnolle.rdbmslibraryclient.model;

/**
 * Representation of an Author.
 * with auId and name attributes
 * @author adnolle@kth.se
 */
public class Author {
    private final int auId;
    private final String name;

    public Author(int auId, String name) {
        this.auId = auId;
        this.name = name;
    }

    public Author(String name) {
        this(-1, name);
    }

    public int getAuId() {
        return auId;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() { return auId + ", " + name; }
}