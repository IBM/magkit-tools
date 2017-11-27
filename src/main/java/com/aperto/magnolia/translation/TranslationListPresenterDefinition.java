package com.aperto.magnolia.translation;

import info.magnolia.ui.workbench.list.ListPresenter;

import static info.magnolia.ui.workbench.list.ListPresenterDefinition.VIEW_TYPE;

/**
 * Translation list presenter definition.
 *
 * @author diana.racho (Aperto AG)
 */
public class TranslationListPresenterDefinition extends AbstractListPresenterDefinition {

    public TranslationListPresenterDefinition() {
        super(ListPresenter.class, VIEW_TYPE);
    }
}