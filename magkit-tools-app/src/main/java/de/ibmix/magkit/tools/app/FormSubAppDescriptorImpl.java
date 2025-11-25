package de.ibmix.magkit.tools.app;

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

import info.magnolia.i18nsystem.I18nable;
import info.magnolia.ui.api.app.SubAppDescriptorKeyGenerator;
import info.magnolia.ui.api.app.registry.ConfiguredSubAppDescriptor;
import info.magnolia.ui.form.definition.FormDefinition;

/**
 * Implementation of FormSubAppDescriptor for form-based sub-applications.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Stores and provides access to form definitions</li>
 *   <li>Extends ConfiguredSubAppDescriptor with form support</li>
 *   <li>Supports i18n key generation for sub-app descriptors</li>
 * </ul>
 *
 * @author frank.sommer
 * @since 2017-01-12
 */
@I18nable(keyGenerator = SubAppDescriptorKeyGenerator.class)
public class FormSubAppDescriptorImpl extends ConfiguredSubAppDescriptor implements FormSubAppDescriptor {
    private FormDefinition _form;

    /**
     * Returns the form definition for this sub-application.
     *
     * @return the form definition
     */
    @Override
    public FormDefinition getForm() {
        return _form;
    }

    /**
     * Sets the form definition for this sub-application.
     *
     * @param form the form definition to set
     */
    public void setForm(final FormDefinition form) {
        _form = form;
    }
}
