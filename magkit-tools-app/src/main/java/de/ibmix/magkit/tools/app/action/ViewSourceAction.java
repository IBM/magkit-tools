package de.ibmix.magkit.tools.app.action;

/*-
 * #%L
 * magkit-tools-app
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

import com.vaadin.server.Page;
import info.magnolia.link.LinkUtil;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Action to display the rendering output for a node.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Generates an external link for the underlying JCR node</li>
 *   <li>Opens the link in a new browser tab for source/output inspection</li>
 * </ul>
 * <p><strong>Error Handling:</strong></p>
 * Silently ignores execution if node or link is not available.
 *
 * @author philipp.guettler
 * @since 2014-05-23
 */
public class ViewSourceAction extends AbstractAction<ViewSourceActionDefinition> {

    private final AbstractJcrNodeAdapter _item;

    /**
     * Constructs a new ViewSourceAction instance.
     *
     * @param definition the action definition
     * @param item the JCR node adapter for the target node
     */
    @Inject
    public ViewSourceAction(ViewSourceActionDefinition definition, AbstractJcrNodeAdapter item) {
        super(definition);
        _item = item;
    }

    /**
     * Executes the action by creating an external link and opening it in a new browser window.
     */
    @Override
    public void execute() {
        if (_item != null && _item.getJcrItem() != null) {
            final String externalLink = LinkUtil.createExternalLink(_item.getJcrItem());
            if (isNotBlank(externalLink)) {
                Page page = Page.getCurrent();
                page.open(externalLink, "_blank", false);
            }
        }
    }
}
