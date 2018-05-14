package com.aperto.magnolia.edittools.action;

import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Action used to open a node in a new location.
 *
 * @author jean-charles.robert
 * @since 08.05.18
 */
public class OpenNodeLocationAction extends AbstractOpenLocationAction {

    private final JcrItemAdapter _item;

    @Inject
    public OpenNodeLocationAction(final AbstractOpenLocationActionDefinition definition, JcrItemAdapter item, LocationController locationController) {
        super(definition, locationController);
        _item = item;
    }

    @Override
    protected String getNodePath() throws ActionExecutionException {
        try {
            Item jcrItem = _item.getJcrItem();
            if (jcrItem.isNode()) {
                Node node = (Node) jcrItem;
                if (node == null) {
                    throw new ActionExecutionException("Not able to resolve node from " + _item.getJcrItem().getPath());
                }
                return node.getPath();
            }
        } catch (RepositoryException e) {
            throw new ActionExecutionException(e);
        }
        return StringUtils.EMPTY;
    }
}
