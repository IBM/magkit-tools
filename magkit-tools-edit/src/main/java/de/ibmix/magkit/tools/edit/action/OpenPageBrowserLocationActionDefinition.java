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

import info.magnolia.ui.api.action.ActionType;

/**
 * Action definition for opening a page in the browser sub-app.
 * This definition is automatically registered with the action type "openPageBrowserLocation" and
 * uses {@link OpenPageBrowserLocationAction} as its implementation.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Navigates to the page browser view for the selected node</li>
 * <li>Registered as action type "openPageBrowserLocation" for use in Magnolia UI configuration</li>
 * </ul>
 *
 * @author Philipp Güttler (IBM iX)
 * @see OpenPageBrowserLocationAction
 * @since 2021-02-19
 */
@ActionType("openPageBrowserLocation")
public class OpenPageBrowserLocationActionDefinition extends OpenAppViewLocationActionDefinition {

    public OpenPageBrowserLocationActionDefinition() {
        setImplementationClass(OpenPageBrowserLocationAction.class);
    }
}
