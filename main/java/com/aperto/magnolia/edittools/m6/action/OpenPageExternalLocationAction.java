package com.aperto.magnolia.edittools.m6.action;

import com.aperto.magkit.utils.NodeUtils;
import com.google.inject.Inject;
import com.vaadin.server.Page;
import info.magnolia.link.LinkUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;

import javax.jcr.Node;

/**
 * Action to open the preview of content in a new browser window.
 *
 * @author diana.racho (Aperto AG)
 * @since 05.02.15
 */
public class OpenPageExternalLocationAction extends AbstractAction<OpenPageExternalLocationActionDefinition> {

    private final ValueContext<Node> _valueContext;

    @Inject
    public OpenPageExternalLocationAction(final OpenPageExternalLocationActionDefinition definition, final ValueContext<Node> valueContext) {
        super(definition);
        _valueContext = valueContext;
    }

    @Override
    public void execute() {
        _valueContext.getSingle()
            .map(node -> NodeUtils.getAncestorOrSelf(node, NodeUtils.IS_PAGE))
            .map(LinkUtil::createLink)
            .ifPresent(url -> Page.getCurrent().open(url, "_blank"));
    }
}