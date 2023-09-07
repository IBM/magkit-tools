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

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Action to display the rendering output for a node.
 *
 * @author philipp.guettler
 * @since 23.05.2014
 */
public class ViewSourceAction extends AbstractAction<ViewSourceActionDefinition> {

    private final AbstractJcrNodeAdapter _item;

    @Inject
    public ViewSourceAction(ViewSourceActionDefinition definition, AbstractJcrNodeAdapter item) {
        super(definition);
        _item = item;
    }

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
