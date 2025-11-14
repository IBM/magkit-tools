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

import com.vaadin.server.Page;
import de.ibmix.magkit.tools.edit.util.LinkService;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Action that opens the published/public URL of a page in a new browser tab.
 * The public URL is generated using {@link LinkService#getPublicLink(Node)}, which supports
 * site-specific host configurations when extended link generation is enabled.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Opens the public page URL in a new browser tab from the Magnolia AdminCentral</li>
 * <li>Supports site-specific host mapping for multi-site setups</li>
 * <li>Works with the current node selection from the value context</li>
 * </ul>
 *
 * <p><strong>Preconditions:</strong></p>
 * A single node must be selected in the value context for this action to execute.
 *
 * @author Philipp Güttler (IBM iX)
 * @see LinkService#getPublicLink(Node)
 * @since 2021-02-19
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

    /**
     * Executes the action by retrieving the selected node, generating its public URL, and opening it in a new browser tab.
     */
    @Override
    public void execute() {
        _valueContext.getSingle()
            .map(_linkService::getPublicLink)
            .ifPresent(url -> Page.getCurrent().open(url, "_blank"));
    }
}
