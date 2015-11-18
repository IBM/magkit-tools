package com.aperto.magnolia.edittools.rule;

import com.aperto.magnolia.edittools.action.CopyNodeAction;
import com.vaadin.data.Property;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.availability.AbstractElementAvailabilityRule;
import info.magnolia.ui.vaadin.gwt.client.shared.AreaElement;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Checks if clipboard content is addable to current area by comparing the areas available components.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 06.07.2015
 */
public class IsClipboardAddable extends AbstractElementAvailabilityRule<AreaElement> {

    @Inject
    public IsClipboardAddable(final PageEditorPresenter pageEditorPresenter) {
        super(pageEditorPresenter, AreaElement.class);
    }

    @Override
    protected boolean isAvailableForElement(final AreaElement element) {
        boolean result = false;
        Object copyElement = MgnlContext.getAttribute(CopyNodeAction.class.getName(), Context.SESSION_SCOPE);
        if (copyElement instanceof JcrItemAdapter) {
            JcrItemAdapter adapter = (JcrItemAdapter) copyElement;
            Property<?> templateId = adapter.getItemProperty(NodeTypes.Renderable.TEMPLATE);
            if (templateId != null) {
               String[] availableComponents = StringUtils.split(element.getAvailableComponents(), ',');
               if (availableComponents != null && availableComponents.length > 0) {
                   result = Arrays.asList(availableComponents).contains(String.valueOf(templateId.getValue())); 
               }
            }
        }
        return result;
    }
}
