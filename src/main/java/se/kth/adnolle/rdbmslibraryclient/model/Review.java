package se.kth.adnolle.rdbmslibraryclient.model;

import java.util.Date;

public class Review {
    private final String username;
    private final Date date;
    private final int rating;
    private final String text;

    public Review(String username, Date date, int rating, String text) {
        this.username = username;
        this.date = date;
        this.rating = rating;
        this.text = text;
    }

    public String getUsername() { return username; }
    public Date getDate() { return date; }
    public int getRating() { return rating; }
    public String getText() { return text; }

    @Override
    public String toString() {
        return String.format("%d/5 by %s (%s)\n%s", rating, username, date, text);
    }
}