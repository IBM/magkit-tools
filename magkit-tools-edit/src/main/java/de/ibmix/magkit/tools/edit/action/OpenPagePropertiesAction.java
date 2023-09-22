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
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.pages.app.action.browser.EditPagePropertiesAction;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.dialog.actions.OpenDialogActionDefinition;
import info.magnolia.ui.editor.LocaleContext;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Opens the page properties dialog for editing a page node.
 *
 * @author frank.sommer
 * @see EditPagePropertiesAction
 * @since 1.2.3
 */
public class OpenPagePropertiesAction extends EditPagePropertiesAction {

    private final ValueContext<Node> _valueContext;

    @Inject
    // CHECKSTYLE:OFF // parameter count
    public OpenPagePropertiesAction(final OpenDialogActionDefinition definition, final LocaleContext localeContext, final ValueContext<Node> valueContext, final UIComponent parentView,
                                    final I18NAuthoringSupport<Node> i18nAuthoringSupport, final DialogDefinitionRegistry dialogDefinitionRegistry,
                                    final TemplateDefinitionAssignment templateDefinitionAssignment, final I18nizer i18nizer) {
        // CHECKSTYLE:ON
        super(definition, localeContext, valueContext, parentView, i18nAuthoringSupport, dialogDefinitionRegistry, templateDefinitionAssignment, i18nizer);
        _valueContext = valueContext;
    }

    @Override
    public void execute() throws ActionExecutionException {
        _valueContext.getSingle()
            .map(node -> NodeUtils.getAncestorOrSelf(node, NodeUtils.IS_PAGE))
            .ifPresent(_valueContext::set);

        super.execute();
    }

}
