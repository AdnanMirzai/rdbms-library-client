package se.kth.adnolle.rdbmslibraryclient.view;

import se.kth.adnolle.rdbmslibraryclient.model.Book;
import se.kth.adnolle.rdbmslibraryclient.model.SearchMode;

/**
 * Interface defining the events that can be triggered by the View.
 * The Controller implements this interface to react to user actions.
 *
 * @author adnolle@kth.se
 */
public interface IViewListener {

    /**
     * Called when the user wants to connect to DB.
     */
    void onConnectSelected();

    /**
     * Called when the user wants to disconnect from DB.
     */
    void onDisconnectSelected();

    /**
     * Called when the user requests a search for books.
     *
     * @param searchFor The string to search for.
     * @param mode      The mode indicating which field to search.
     */
    void onSearchSelected(String searchFor, SearchMode mode);

    /**
     * Called when the user requests to view all authors for book.
     *
     * @param selectedBook The Book to get authors for.
     */
    void onBookDetailsSelected(Book selectedBook);

    /**
     * Called when the user wants to add a new book to DB.
     */
    void onAddBookSelected();

    /**
     * Called when the user wants to update rating for a book in DB.
     */
    void onReviewBookSelected(Book selectedBook);

    /**
     * Called when the user wants to exit application.
     */
    void onExitSelected();

    /**
     * Called when the user wants login.
     */
    void onLoginSelected();

    /**
     * Called when the user wants logout.
     */
    void onLogoutSelected();

    /**
     * Called when the user wants to delete a book.
     */
    void onRemoveBookSelected();
}
