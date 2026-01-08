package se.kth.adnolle.rdbmslibraryclient.model;

import java.sql.Date;

/**
 * Representation of an object model Author.
 * with auId, name and DOB attributes
 *
 * @author adnolle@kth.se
 */
public class Author {
    private final int auId;
    private final String name;
    private final Date DOB;
    private final String addedByName;

    public Author(int auId, String name, Date DOB, String addedByName) {
        this.auId = auId;
        this.name = name;
        this.DOB = DOB;
        this.addedByName = addedByName;
    }

    public Author(int auID, String name, Date DOB) {
        this(-1, name, DOB, "");
    }

    public int getAuId() {
        return auId;
    }

    public String getName() {
        return name;
    }

    public Date getDOB() {
        return DOB;
    }
    public String getAddedByName() {
        return addedByName;
    }

    @Override
    public String toString() {
        String creator = (addedByName != null && !addedByName.isEmpty()) ? " (Added by: " + addedByName + ")" : "";
        return name + creator;
    }
}