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

import de.ibmix.magkit.core.utils.NodeUtils;
import com.google.inject.Inject;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.location.LocationController;
import org.apache.commons.lang3.StringUtils;

import javax.jcr.Node;

/**
 * Action that navigates to the browser sub-app for the page ancestor of the currently selected node.
 * This action automatically determines the page node from the current selection and opens it in the configured
 * app view (typically tree view).
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Automatically locates the page ancestor of the selected node</li>
 * <li>Navigates to the browser sub-app with the page location</li>
 * <li>Handles nodes at any level within the page hierarchy</li>
 * </ul>
 *
 * <p><strong>Null Handling:</strong></p>
 * If no page ancestor is found, returns an empty string for the node path.
 *
 * @author frank.sommer
 * @since 2016-01-15
 */
public class OpenPageBrowserLocationAction extends OpenAppViewLocationAction {

    private final ValueContext<Node> _valueContext;

    @Inject
    public OpenPageBrowserLocationAction(final OpenPageBrowserLocationActionDefinition definition, final ValueContext<Node> valueContext, final LocationController locationController) {
        super(definition, locationController);
        _valueContext = valueContext;
    }

    /**
     * Determines the node path by finding the page ancestor of the selected node.
     *
     * @return the path of the page node, or an empty string if no page ancestor exists
     */
    @Override
    protected String getNodePath() {
        return _valueContext.getSingle()
            .map(node -> NodeUtils.getAncestorOrSelf(node, NodeUtils.IS_PAGE))
            .map(NodeUtil::getPathIfPossible)
            .orElse(StringUtils.EMPTY);
    }
}
