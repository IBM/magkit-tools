package com.aperto.magnolia.edittools.action;

import com.google.inject.Inject;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static info.magnolia.jcr.util.NodeUtil.getNearestAncestorOfType;
import static info.magnolia.jcr.util.NodeUtil.isNodeType;

/**
 * Action to open the jump to the browser sub app with the current location.
 *
 * @author frank.sommer
 * @since 15.01.16
 */
public class OpenPageLocationAction extends AbstractOpenLocationAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenPageLocationAction.class);

    private final AbstractJcrNodeAdapter _nodeItemToPreview;

    @Inject
    public OpenPageLocationAction(AbstractOpenLocationActionDefinition definition, AbstractJcrNodeAdapter nodeItemToPreview, LocationController locationController) {
        super(definition, locationController);
        _nodeItemToPreview = nodeItemToPreview;
    }

    @Override
    protected String getNodePath() throws ActionExecutionException {
        Node pageNode = _nodeItemToPreview.getJcrItem();
        try {
            if (!isNodeType(pageNode, NodeTypes.Page.NAME)) {
                pageNode = getNearestAncestorOfType(pageNode, NodeTypes.Page.NAME);
            }

            if (pageNode == null) {
                throw new ActionExecutionException("Not able to resolve page node from " + _nodeItemToPreview.getJcrItem().getPath());
            }
            return pageNode.getPath();

        } catch (RepositoryException e) {
            LOGGER.error("Can't get page node for opening browser sub app", e);
            throw new ActionExecutionException(e);
        }
    }
}