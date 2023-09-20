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
import de.ibmix.magkit.tools.edit.m6.action.OpenPageBrowserLocationAction;
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
 * @deprecated for Magnolia 6 use {@link OpenPageBrowserLocationAction}
 */
@Deprecated
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
