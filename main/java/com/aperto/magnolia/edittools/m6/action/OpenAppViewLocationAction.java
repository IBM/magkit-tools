package com.aperto.magnolia.edittools.m6.action;

import com.google.inject.Inject;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.ContentBrowserSubApp.BrowserLocation;

/**
 * Action to open the current node in a new browser location. See {@link info.magnolia.ui.contentapp.action.OpenLocationAction}
 *
 * @author jean-charles.robert
 * @since 09.05.18
 */
public abstract class OpenAppViewLocationAction extends AbstractAction<OpenAppViewLocationActionDefinition> {

    public static final String TREE_VIEW = "treeview";
    private static final String VIEW_SEPARATOR = ":";

    private final LocationController _locationController;

    @Inject
    public OpenAppViewLocationAction(final OpenAppViewLocationActionDefinition definition, final LocationController locationController) {
        super(definition);
        _locationController = locationController;
    }

    @Override
    public void execute() throws ActionExecutionException {
        BrowserLocation location = new BrowserLocation(getDefinition().getAppName(), getDefinition().getSubAppId(),
            getNodePath() + VIEW_SEPARATOR + getDefinition().getViewType() + VIEW_SEPARATOR
        );

        _locationController.goTo(location);
    }

    /**
     * Implement this method to get the path to open.
     */
    protected abstract String getNodePath() throws ActionExecutionException;

}
