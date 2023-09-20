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

import de.ibmix.magkit.core.utils.NodeUtils;
import de.ibmix.magkit.tools.edit.m6.rule.IsPagePropertiesEditableRule;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.availability.AbstractElementAvailabilityRule;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.core.utils.NodeUtils.isNodeType;
import static info.magnolia.jcr.util.NodeUtil.getNearestAncestorOfType;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Generic class to check if page properties is available. We use a non-Java-Generic class to handle different elements like {@link info.magnolia.ui.vaadin.gwt.client.shared.PageElement PageElement}, {@link
 * info.magnolia.ui.vaadin.gwt.client.shared.AreaElement AreaElement}, or {@link info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement ComponentElement}.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 30.06.2015
 * @deprecated for Magnolia 6 use {@link IsPagePropertiesEditableRule}
 */
@Deprecated
public class IsElementEditableRule extends AbstractElementAvailabilityRule<AbstractElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsElementEditableRule.class);

    private TemplateDefinitionRegistry _templateDefinitionRegistry;

    @Inject
    public IsElementEditableRule(final PageEditorPresenter pageEditorPresenter) {
        super(pageEditorPresenter, AbstractElement.class);
    }

    @Override
    protected boolean isAvailableForElement(final AbstractElement element) {
        boolean result = false;
        if (element instanceof PageElement) {
            result = element.getDialog() != null;
        } else if (isValidElement(element)) {
            try {
                Node pageNode = SessionUtil.getNode(element.getWorkspace(), element.getPath());
                if (pageNode != null) {
                    if (!isNodeType(pageNode, NodeTypes.Page.NAME)) {
                        pageNode = getNearestAncestorOfType(pageNode, NodeTypes.Page.NAME);
                    }
                    DefinitionProvider<TemplateDefinition> templateDefinition = _templateDefinitionRegistry.getProvider(NodeUtils.getTemplate(pageNode));
                    result = templateDefinition.get().getDialog() != null && (templateDefinition.get().getEditable() == null || templateDefinition.get().getEditable());
                }
            } catch (RepositoryException e) {
                LOGGER.debug("Unable to check page template for dialog");
            }
        }

        return result;
    }

    /**
     * Checks if the current element is not inherited. There is obviously no simple check for that.
     *
     * @param element current element (component or area)
     * @return is allowed element
     */
    private boolean isValidElement(final AbstractElement element) {
        return element != null && isNotBlank(element.getPath()) && isNotBlank(element.getWorkspace()) && (!(element instanceof ComponentElement) || ((ComponentElement) element).getEditable());
    }

    @Inject
    public void setTemplateDefinitionRegistry(final TemplateDefinitionRegistry templateDefinitionRegistry) {
        _templateDefinitionRegistry = templateDefinitionRegistry;
    }
}
