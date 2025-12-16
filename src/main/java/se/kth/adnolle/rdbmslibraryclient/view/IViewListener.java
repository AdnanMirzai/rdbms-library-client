package se.kth.adnolle.rdbmslibraryclient.view;

import se.kth.adnolle.rdbmslibraryclient.model.Book;
import se.kth.adnolle.rdbmslibraryclient.model.SearchMode;

public interface IViewListener {

    // Database connection
    void onConnectSelected();
    void onDisconnectSelected();

    // Search
    void onSearchSelected(String searchFor, SearchMode mode);

    // Detailed info about Author
    void onAuthorDetailsSelected(Book selectedBook);

    // Add book
    void onAddBookSelected();

    // Rate book
    void onRateBookSelected(Book selectedBook);

    // Exit
    void onExitSelected();
}
