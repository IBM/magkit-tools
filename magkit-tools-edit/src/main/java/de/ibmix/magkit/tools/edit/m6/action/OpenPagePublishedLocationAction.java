package de.ibmix.magkit.tools.edit.m6.action;

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

import de.ibmix.magkit.tools.edit.util.LinkService;
import com.vaadin.server.Page;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Opens a new tab with location generated by {@link LinkService#getPublicLink(String)}.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 19.02.2021
 */
public class OpenPagePublishedLocationAction extends AbstractAction<ConfiguredActionDefinition> {

    private final ValueContext<Node> _valueContext;
    private final LinkService _linkService;

    @Inject
    public OpenPagePublishedLocationAction(final OpenPagePublishedLocationActionDefinition definition, final ValueContext<Node> valueContext, final LinkService linkService) {
        super(definition);
        _valueContext = valueContext;
        _linkService = linkService;
    }

    @Override
    public void execute() {
        _valueContext.getSingle()
            .map(NodeUtil::getPathIfPossible)
            .map(_linkService::getPublicLink)
            .ifPresent(url -> Page.getCurrent().open(url, "_blank"));
    }
}
