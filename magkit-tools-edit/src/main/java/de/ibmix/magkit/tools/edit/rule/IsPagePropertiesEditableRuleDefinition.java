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

import info.magnolia.ui.api.availability.AvailabilityRuleType;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;

/**
 * Configuration definition for the {@link IsPagePropertiesEditableRule} availability rule.
 * This definition is automatically registered with the availability rule type "isPagePropertiesEditable"
 * for use in Magnolia UI action configurations.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Registered as availability rule type "isPagePropertiesEditable"</li>
 * <li>Used to control visibility of page property editing actions</li>
 * <li>No additional configuration properties beyond the base rule definition</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * availability:
 *   rules:
 *     - name: isPagePropertiesEditable
 *       implementationClass: de.ibmix.magkit.tools.edit.rule.IsPagePropertiesEditableRule
 * </pre>
 *
 * @author Philipp Güttler (IBM iX)
 * @see IsPagePropertiesEditableRule
 * @since 2021-02-19
 */
@AvailabilityRuleType("isPagePropertiesEditable")
public class IsPagePropertiesEditableRuleDefinition extends ConfiguredAvailabilityRuleDefinition {

    public IsPagePropertiesEditableRuleDefinition() {
        setImplementationClass(IsPagePropertiesEditableRule.class);
    }
}
