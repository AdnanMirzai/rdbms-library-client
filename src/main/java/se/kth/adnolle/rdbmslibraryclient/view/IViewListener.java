package se.kth.adnolle.rdbmslibraryclient.view;

import se.kth.adnolle.rdbmslibraryclient.model.SearchMode;

public interface IViewListener {

    // Database connection
    void onConnectSelected();
    void onDisconnectSelected();

    // Search
    void onSearchSelected(String text, SearchMode mode);

    // Add book
    void addBookSelected();

    // Rate book
    void rateBookSelected();

    // Detailed info about book
    void DetailsSelected();
}
