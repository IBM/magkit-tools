package com.aperto.magnolia.translation;

import info.magnolia.ui.workbench.search.SearchPresenter;

import static info.magnolia.ui.workbench.search.SearchPresenterDefinition.VIEW_TYPE;

/**
 * Translation configured definition for a content view presenting search results.
 *
 * @author frank.sommer
 * @since 1.0.3
 */
public class TranslationSearchPresenterDefinition extends AbstractListPresenterDefinition {

    public TranslationSearchPresenterDefinition() {
        super(SearchPresenter.class, VIEW_TYPE);
    }
}
