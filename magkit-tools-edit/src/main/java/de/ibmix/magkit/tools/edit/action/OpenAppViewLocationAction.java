package de.ibmix.magkit.tools.edit.action;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.ContentBrowserSubApp.BrowserLocation;

/**
 * Abstract action for opening JCR nodes in specific app views within the Magnolia AdminCentral.
 * This action navigates to a browser location with a specific view type (e.g., tree view, list view).
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Navigate to specific app and sub-app locations within Magnolia AdminCentral</li>
 * <li>Support for different view types (tree view, list view, etc.)</li>
 * <li>Extensible through abstract method for node path resolution</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * Extend this class and implement the {@link #getNodePath()} method to define which node path to navigate to.
 *
 * @author jean-charles.robert
 * @see info.magnolia.ui.contentapp.action.OpenLocationAction
 * @since 2018-05-09
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

    /**
     * Executes the action by constructing a browser location and navigating to it.
     *
     * @throws ActionExecutionException if the action execution fails
     */
    @Override
    public void execute() throws ActionExecutionException {
        BrowserLocation location = new BrowserLocation(getDefinition().getAppName(), getDefinition().getSubAppId(),
            getNodePath() + VIEW_SEPARATOR + getDefinition().getViewType() + VIEW_SEPARATOR
        );

        _locationController.goTo(location);
    }

    /**
     * Returns the JCR path of the node to navigate to. Subclasses must implement this method
     * to define the target node path based on their specific requirements.
     *
     * @return the JCR node path as a string
     * @throws ActionExecutionException if the node path cannot be determined
     */
    protected abstract String getNodePath() throws ActionExecutionException;

}
