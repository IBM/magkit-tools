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
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.app.SubAppDescriptorKeyGenerator;
import info.magnolia.ui.form.definition.FormDefinition;

/**
 * Sub-application descriptor interface for form-based sub-applications.
 * <p>
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Extends standard SubAppDescriptor with form definition support</li>
 *   <li>Provides access to the form definition for the sub-application</li>
 * </ul>
 * <p>
 * <p><strong>Usage:</strong></p>
 * This interface is used by sub-applications that require form input,
 * such as QuerySubApp and VersionPruneSubApp.
 *
 * @author frank.sommer
 * @since 2017-01-12
 */
@I18nable(keyGenerator = SubAppDescriptorKeyGenerator.class)
public interface FormSubAppDescriptor extends SubAppDescriptor {
    /**
     * Returns the form definition for this sub-application.
     *
     * @return the form definition
     */
    FormDefinition getForm();
}
