package de.ibmix.magkit.tools.edit.rule;

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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.pages.app.detail.PageEditorStatus;
import info.magnolia.pages.app.detail.action.availability.IsElementEditableRule;
import info.magnolia.pages.app.detail.action.availability.IsElementEditableRuleDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import jakarta.inject.Inject;

import javax.jcr.Node;

import static de.ibmix.magkit.core.utils.NodeUtils.IS_PAGE;
import static de.ibmix.magkit.core.utils.NodeUtils.getAncestorOrSelf;
import static de.ibmix.magkit.core.utils.NodeUtils.getNodeByReference;
import static de.ibmix.magkit.core.utils.NodeUtils.getTemplate;

/**
 * Availability rule that determines if page properties can be edited for the current element in the page editor.
 * This rule extends {@link IsElementEditableRule} and verifies that the page has a configured dialog
 * and is marked as editable in its template definition.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Checks if the page template has a dialog configured</li>
 * <li>Verifies the template's editable property (defaults to true if not set)</li>
 * <li>Works with any element type (page, area, component) by finding the page ancestor</li>
 * <li>Integrates with Magnolia's availability rule system</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * This rule is typically used in action definitions to control the availability of the "Edit Page Properties"
 * action in the page editor context menu.
 *
 * <p><strong>Evaluation Criteria:</strong></p>
 * Returns true if:
 * <ul>
 * <li>The current element has a page ancestor</li>
 * <li>The page template has a dialog defined</li>
 * <li>The page template's editable property is null or true</li>
 * </ul>
 *
 * @author Philipp Güttler (IBM iX)
 * @see PageElement
 * @see info.magnolia.ui.vaadin.gwt.client.shared.AreaElement
 * @see ComponentElement
 * @since 2015-06-30
 */
public class IsPagePropertiesEditableRule extends IsElementEditableRule {

    private final PageEditorStatus _pageEditorStatus;
    private final TemplateDefinitionRegistry _templateDefinitionRegistry;

    @Inject
    public IsPagePropertiesEditableRule(final AvailabilityDefinition availabilityDefinition, final IsElementEditableRuleDefinition ruleDefinition, final PageEditorStatus pageEditorStatus,
                                        final TemplateDefinitionRegistry templateDefinitionRegistry) {
        super(availabilityDefinition, ruleDefinition, pageEditorStatus);
        _pageEditorStatus = pageEditorStatus;
        _templateDefinitionRegistry = templateDefinitionRegistry;
    }

    /**
     * Evaluates if the page properties edit action is available for the given element.
     * Checks that the page has a dialog configured and is marked as editable.
     *
     * @param element the page editor element to evaluate
     * @return true if page properties can be edited, false otherwise
     */
    @Override
    protected boolean isAvailableFor(final AbstractElement element) {
        Node node = getAncestorOrSelf(getNodeByReference(RepositoryConstants.WEBSITE, _pageEditorStatus.getNodePath()), IS_PAGE);

        if (node == null) {
            return false;
        }

        DefinitionProvider<TemplateDefinition> templateDefinition = _templateDefinitionRegistry.getProvider(getTemplate(node));

        return templateDefinition.get().getDialog() != null && (templateDefinition.get().getEditable() == null || templateDefinition.get().getEditable());
    }
}
