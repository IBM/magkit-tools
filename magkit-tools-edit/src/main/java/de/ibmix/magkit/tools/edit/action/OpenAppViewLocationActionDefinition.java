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


import info.magnolia.ui.contentapp.action.OpenLocationActionDefinition;

/**
 * Configuration definition for {@link OpenAppViewLocationAction}.
 * This definition extends the standard OpenLocationActionDefinition with an additional view type property
 * to specify which view should be displayed when navigating to a location (e.g., tree view, list view).
 *
 * <p><strong>Configuration Properties:</strong></p>
 * <ul>
 * <li>viewType - the type of view to display (defaults to tree view)</li>
 * <li>All properties inherited from {@link OpenLocationActionDefinition}</li>
 * </ul>
 *
 * @author jean-charles.robert
 * @see OpenLocationActionDefinition
 * @see OpenAppViewLocationAction
 * @since 2018-05-14
 */
public class OpenAppViewLocationActionDefinition extends OpenLocationActionDefinition {

    private String _viewType = OpenAppViewLocationAction.TREE_VIEW;

    /**
     * Returns the configured view type for the browser location.
     *
     * @return the view type (e.g., "treeview")
     */
    public String getViewType() {
        return _viewType;
    }

    /**
     * Sets the view type for the browser location.
     *
     * @param viewType the view type to set (e.g., "treeview", "listview")
     */
    public void setViewType(String viewType) {
        _viewType = viewType;
    }
}
