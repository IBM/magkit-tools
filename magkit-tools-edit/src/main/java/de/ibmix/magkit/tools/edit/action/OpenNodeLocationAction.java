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
