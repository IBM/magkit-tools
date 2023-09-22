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
import com.vaadin.server.Page;
import info.magnolia.link.LinkUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;

import javax.jcr.Node;

/**
 * Action to open the preview of content in a new browser window.
 *
 * @author diana.racho (IBM iX)
 * @since 05.02.15
 */
public class OpenPageExternalLocationAction extends AbstractAction<OpenPageExternalLocationActionDefinition> {

    private final ValueContext<Node> _valueContext;

    @Inject
    public OpenPageExternalLocationAction(final OpenPageExternalLocationActionDefinition definition, final ValueContext<Node> valueContext) {
        super(definition);
        _valueContext = valueContext;
    }

    @Override
    public void execute() {
        _valueContext.getSingle()
            .map(node -> NodeUtils.getAncestorOrSelf(node, NodeUtils.IS_PAGE))
            .map(LinkUtil::createLink)
            .ifPresent(url -> Page.getCurrent().open(url, "_blank"));
    }
}
