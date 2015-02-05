package com.aperto.magnolia.edittools.action;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.framework.action.DuplicateNodeAction;
import info.magnolia.ui.framework.action.DuplicateNodeActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;

import javax.inject.Named;

/**
 * Duplicate component action.
 *
 * @author frank.sommer
 * @since 31.03.14
 */
public class DuplicateComponentAction extends DuplicateNodeAction {

    public DuplicateComponentAction(final DuplicateNodeActionDefinition definition, final JcrItemAdapter item, @Named(SubAppEventBus.NAME) EventBus eventBus) {
        super(definition, item, eventBus);
    }
}
