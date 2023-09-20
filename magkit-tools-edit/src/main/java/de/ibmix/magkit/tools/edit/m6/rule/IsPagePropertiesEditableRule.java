package de.ibmix.magkit.tools.edit.m6.rule;

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

import javax.inject.Inject;
import javax.jcr.Node;

import static de.ibmix.magkit.core.utils.NodeUtils.IS_PAGE;
import static de.ibmix.magkit.core.utils.NodeUtils.getAncestorOrSelf;
import static de.ibmix.magkit.core.utils.NodeUtils.getNodeByReference;
import static de.ibmix.magkit.core.utils.NodeUtils.getTemplate;

/**
 * Generic class to check if page properties is available. We use a non-Java-Generic class to handle different elements like {@link PageElement PageElement}, {@link
 * info.magnolia.ui.vaadin.gwt.client.shared.AreaElement AreaElement}, or {@link ComponentElement ComponentElement}.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 30.06.2015
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
