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

import de.ibmix.magkit.tools.edit.m6.action.OpenPagePropertiesActionDefinition;
import info.magnolia.pages.app.action.EditElementActionDefinition;

/**
 * Action definition for {@link EditPageAction EditPageAction}.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 30.06.2015
 * @deprecated for Magnolia 6 use {@link OpenPagePropertiesActionDefinition}
 */
@Deprecated
public class EditPageActionDefinition extends EditElementActionDefinition {

    public EditPageActionDefinition() {
        setImplementationClass(EditPageAction.class);
    }
}
