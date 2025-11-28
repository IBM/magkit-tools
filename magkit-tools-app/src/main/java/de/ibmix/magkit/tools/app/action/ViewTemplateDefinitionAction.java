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

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;

import javax.inject.Inject;

import static de.ibmix.magkit.core.utils.NodeUtils.getTemplate;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.substringAfter;
import static org.apache.commons.lang.StringUtils.substringBefore;

/**
 * Action to navigate to template definition in config app.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Extracts template identifier from the selected JCR node</li>
 *   <li>Parses module and path parts of the template ID</li>
 *   <li>Navigates to the template definition in the definitions app</li>
 * </ul>
 * <p><strong>Null Handling:</strong></p>
 * Execution is skipped if node or template identifier is missing.
 *
 * @author philipp.guettler
 * @since 2014-05-23
 */
public class ViewTemplateDefinitionAction extends AbstractAction<ViewTemplateDefinitionActionDefinition> {
    private final LocationController _locationController;
    private final AbstractJcrNodeAdapter _item;

    /**
     * Constructs a new ViewTemplateDefinitionAction instance.
     *
     * @param definition the action definition
     * @param locationController the location controller for navigation
     * @param item the JCR node adapter for the target node
     */
    @Inject
    public ViewTemplateDefinitionAction(final ViewTemplateDefinitionActionDefinition definition, final LocationController locationController, final AbstractJcrNodeAdapter item) {
        super(definition);
        _locationController = locationController;
        _item = item;
    }

    /**
     * Executes the action by retrieving the template ID and navigating to its definition.
     */
    @Override
    public void execute() {
        if (_item != null && _item.getJcrItem() != null) {
            String templateId = getTemplate(_item.getJcrItem());
            if (isNotBlank(templateId)) {
                String moduleName = substringBefore(templateId, ":");
                String path = substringAfter(templateId, ":");
                _locationController.goTo(new BrowserLocation("definitions-app", "overview", "template~" + moduleName + "~" + path));
            }
        }
    }
}
