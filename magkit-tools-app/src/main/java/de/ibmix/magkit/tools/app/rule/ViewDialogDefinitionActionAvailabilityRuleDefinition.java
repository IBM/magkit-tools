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

import info.magnolia.ui.api.availability.ConfiguredAvailabilityRuleDefinition;

/**
 * Configuration definition for {@link ViewDialogDefinitionActionAvailabilityRule}.
 * <p>
 * Sets the implementation class to ViewDialogDefinitionActionAvailabilityRule.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 2014-11-17
 */
public class ViewDialogDefinitionActionAvailabilityRuleDefinition extends ConfiguredAvailabilityRuleDefinition {

    public ViewDialogDefinitionActionAvailabilityRuleDefinition() {
        setImplementationClass(ViewDialogDefinitionActionAvailabilityRule.class);
    }
}
