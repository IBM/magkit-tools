package com.aperto.magnolia.edittools.action;

import com.google.inject.Inject;
import com.vaadin.server.Page;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static info.magnolia.jcr.util.NodeUtil.getNearestAncestorOfType;
import static info.magnolia.jcr.util.NodeUtil.isNodeType;

/**
 * Action to open the preview of content in a new browser window.
 *
 * @author diana.racho (Aperto AG)
 * @since 05.02.15
 */
public class OpenPreviewNewWindowAction extends AbstractAction<ConfiguredActionDefinition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenPreviewNewWindowAction.class);

    private TemplatingFunctions _stkTemplatingFunctions;
    private final AbstractJcrNodeAdapter _nodeItemToPreview;

    @Inject
    public OpenPreviewNewWindowAction(ConfiguredActionDefinition definition, TemplatingFunctions stkTemplatingFunctions, AbstractJcrNodeAdapter nodeItemToPreview) {
        super(definition);
        _stkTemplatingFunctions = stkTemplatingFunctions;
        _nodeItemToPreview = nodeItemToPreview;
    }

    @Override
    public void execute() throws ActionExecutionException {
        Node pageNode = _nodeItemToPreview.getJcrItem();
        try {
            if (!isNodeType(pageNode, NodeTypes.Page.NAME)) {
                pageNode = getNearestAncestorOfType(pageNode, NodeTypes.Page.NAME);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Can't get page node for preview", e);
        }
        Page.getCurrent().open(_stkTemplatingFunctions.link(pageNode), "_blank");
    }
}