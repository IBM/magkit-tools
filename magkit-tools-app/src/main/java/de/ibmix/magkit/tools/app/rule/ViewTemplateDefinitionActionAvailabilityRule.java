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

import de.ibmix.magkit.tools.app.action.ViewTemplateDefinitionAction;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;

import static de.ibmix.magkit.core.utils.NodeUtils.getNodeByIdentifier;
import static de.ibmix.magkit.core.utils.NodeUtils.getTemplate;

/**
 * Rule for enabling/disabling {@link ViewTemplateDefinitionAction}.
 *
 * @author philipp.guettler
 * @since 23.05.2014
 */
public class ViewTemplateDefinitionActionAvailabilityRule extends AbstractAvailabilityRule {

    @Override
    protected boolean isAvailableForItem(final Object itemId) {
        return itemId instanceof JcrNodeItemId && getTemplate(getNodeByIdentifier(((JcrNodeItemId) itemId).getWorkspace(), ((JcrNodeItemId) itemId).getUuid())) != null;
    }
}
