package de.ibmix.magkit.tools.app.rule;

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

import de.ibmix.magkit.tools.app.action.ViewDialogDefinitionAction;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;

import javax.inject.Inject;
import javax.jcr.Node;

import static de.ibmix.magkit.core.utils.NodeUtils.getNodeByIdentifier;
import static de.ibmix.magkit.core.utils.NodeUtils.getTemplate;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Rule for enabling/disabling {@link ViewDialogDefinitionAction}.
 *
 * @author philipp.guettler
 * @since 23.05.2014
 */
public class ViewDialogDefinitionActionAvailabilityRule extends AbstractAvailabilityRule {

    /**
     * Returns the template definition registry, lazy-loading it if needed.
     *
     * @return the template definition registry
     */
    private TemplateDefinitionRegistry getTemplateRegistry() {
        if (_templateRegistry == null) {
            _templateRegistry = Components.getComponent(TemplateDefinitionRegistry.class);
        }
        return _templateRegistry;
    }

    /**
     * Sets the template definition registry.
     *
     * @param templateRegistry the template registry to set
     */
    @Inject
    public void setTemplateRegistry(TemplateDefinitionRegistry templateRegistry) {
        _templateRegistry = templateRegistry;
    }

    @Inject
    private TemplateDefinitionRegistry _templateRegistry;

    /**
     * Checks if the action is available for the given item.
     * Returns true if the item is a JCR node with a template that has a dialog definition.
     *
     * @param itemId the item to check
     * @return true if the action should be available, false otherwise
     */
    @Override
    protected boolean isAvailableForItem(Object itemId) {
        boolean isAvailable = false;
        if (itemId instanceof JcrNodeItemId) {
            JcrNodeItemId item = (JcrNodeItemId) itemId;
            Node node = getNodeByIdentifier(item.getWorkspace(), item.getUuid());
            String template = getTemplate(node);
            if (isNotBlank(template)) {
                DefinitionProvider<TemplateDefinition> definition = getTemplateRegistry().getProvider(template);
                isAvailable = isNotBlank(definition.get().getDialog());
            }
        }
        return isAvailable;
    }
}
