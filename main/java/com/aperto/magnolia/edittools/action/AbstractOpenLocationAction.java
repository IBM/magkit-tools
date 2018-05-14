package com.aperto.magnolia.edittools.action;

import com.google.inject.Inject;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;

/**
 * Action to open the current node in a new browser location.
 *
 * @author jean-charles.robert
 * @see {@link info.magnolia.ui.framework.action.OpenLocationAction}
 * @since 09.05.18
 */
public abstract class AbstractOpenLocationAction extends AbstractAction<AbstractOpenLocationActionDefinition> {

    public static final String TREE_VIEW = "treeview";
    private static final String VIEW_SEPARATOR = ":";

    private final LocationController _locationController;

    @Inject
    public AbstractOpenLocationAction(AbstractOpenLocationActionDefinition definition, LocationController locationController) {
        super(definition);
        _locationController = locationController;
    }

    @Override
    public void execute() throws ActionExecutionException {
        final String appName = getDefinition().getAppName();
        final String subAppId = getDefinition().getSubAppId();
        final String viewType = getDefinition().getViewType();
        String nodePath = getNodePath();

        Location location = new BrowserLocation(appName, subAppId, buildParameter(viewType, nodePath));
        _locationController.goTo(location);
    }

    /**
     * Implement this method to get the path to open.
     */
    protected abstract String getNodePath() throws ActionExecutionException;

    private static String buildParameter(String viewType, String nodePath) {
        StringBuilder sb = new StringBuilder();
        sb.append(nodePath);
        sb.append(VIEW_SEPARATOR).append(viewType).append(VIEW_SEPARATOR);
        return sb.toString();
    }

}
