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

import de.ibmix.magkit.tools.edit.m6.action.OpenPageBrowserLocationActionDefinition;

/**
 * Action definition for {@link OpenPageLocationAction}.
 *
 * @author jean-charles.robert
 * @since 08.05.18
 * @deprecated for Magnolia 6 use {@link OpenPageBrowserLocationActionDefinition}
 */
@Deprecated
public class OpenPageLocationActionDefinition extends AbstractOpenLocationActionDefinition {

    public OpenPageLocationActionDefinition() {
        setImplementationClass(OpenPageLocationAction.class);
    }

}
