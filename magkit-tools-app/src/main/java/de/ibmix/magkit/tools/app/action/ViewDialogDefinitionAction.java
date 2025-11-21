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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;

import static de.ibmix.magkit.core.utils.NodeUtils.getTemplate;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;

/**
 * Action to navigate to dialog definition for a node.
 *
 * @author philipp.guettler
 * @since 23.05.2014
 */
public class ViewDialogDefinitionAction extends AbstractAction<ViewDialogDefinitionActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewDialogDefinitionAction.class);

    private final AbstractJcrNodeAdapter _item;
    private final LocationController _locationController;
    private final TemplateDefinitionRegistry _templateRegistry;

    @Inject
    public ViewDialogDefinitionAction(ViewDialogDefinitionActionDefinition definition, AbstractJcrNodeAdapter item, LocationController locationController, final TemplateDefinitionRegistry templateRegistry) {
        super(definition);
        _locationController = locationController;
        _templateRegistry = templateRegistry;
        _item = item;
    }

    @Override
    public void execute() {
        if (_item != null && _item.getJcrItem() != null && _templateRegistry != null) {
            final Node node = _item.getJcrItem();
            final DefinitionProvider<TemplateDefinition> templateDefinition = _templateRegistry.getProvider(getTemplate(node));
            if (templateDefinition != null) {
                goToDialogDefinition(templateDefinition.get().getDialog());
            }
        } else {
            LOGGER.debug("Unable to execute action: element, item, or template registry not found");
        }
    }

    /**
     * Navigates to the dialog definition in the definitions app.
     *
     * @param dialogId the dialog ID in format "module:path"
     */
    private void goToDialogDefinition(final String dialogId) {
        if (isNotBlank(dialogId)) {
            String moduleName = substringBefore(dialogId, ":");
            String path = substringAfter(dialogId, ":");
            _locationController.goTo(new BrowserLocation("definitions-app", "overview", "dialog~" + moduleName + "~" + path));
        }
    }
}
