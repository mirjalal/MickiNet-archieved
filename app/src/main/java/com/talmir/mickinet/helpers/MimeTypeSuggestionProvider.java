package com.talmir.mickinet.helpers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * @author mirjalal
 * @since 4/18/2017
 */
public class MimeTypeSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.talmir.mickinet.helpers.MimeTypeSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES/* | DATABASE_MODE_2LINES*/;

    public MimeTypeSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
