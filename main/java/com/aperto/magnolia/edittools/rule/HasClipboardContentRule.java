package com.aperto.magnolia.edittools.rule;

import com.aperto.magnolia.edittools.action.CopyNodeAction;
import info.magnolia.context.MgnlContext;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.availability.AbstractElementAvailabilityRule;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import static info.magnolia.context.Context.SESSION_SCOPE;

/**
 * Checks if session clipboard has content and removes obsolete references from session if found.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 06.07.2015
 */
public class HasClipboardContentRule extends AbstractElementAvailabilityRule<AbstractElement> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HasClipboardContentRule.class);

    @Inject
    public HasClipboardContentRule(final PageEditorPresenter pageEditorPresenter) {
        super(pageEditorPresenter, AbstractElement.class);
    }

    @Override
    protected boolean isAvailableForElement(final AbstractElement element) {
        boolean itemExists = false;
        Object object = MgnlContext.getAttribute(CopyNodeAction.class.getName(), SESSION_SCOPE);
        boolean hasContent = object != null;

        if (object instanceof JcrItemAdapter && ((JcrItemAdapter) object).isNode()) {
            Item jcrItem = ((JcrItemAdapter) object).getJcrItem();
            try {
                itemExists = jcrItem != null && jcrItem.getSession().itemExists(jcrItem.getPath());
            } catch (RepositoryException e) {
                LOGGER.debug("Unable to get node from item", e);
            }
        }

        if (hasContent && !itemExists) {
            MgnlContext.removeAttribute(CopyNodeAction.class.getName(), SESSION_SCOPE);
        }

        return itemExists;
    }
}
