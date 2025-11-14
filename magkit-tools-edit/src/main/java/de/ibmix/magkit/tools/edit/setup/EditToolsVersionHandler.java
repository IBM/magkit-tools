package de.ibmix.magkit.tools.edit.setup;

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

import de.ibmix.magkit.setup.BootstrapModuleVersionHandler;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;

import java.util.List;

/**
 * Version handler for the Edit Tools module, managing installation and update tasks.
 * This handler extends {@link BootstrapModuleVersionHandler} to handle module installation,
 * updates, and configuration bootstrapping.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Conditionally installs status bar configuration during updates</li>
 * <li>Conditionally installs move workspace confirmation configuration during updates</li>
 * <li>Extends standard bootstrap functionality for module setup</li>
 * </ul>
 *
 * <p><strong>Bootstrap Files:</strong></p>
 * <ul>
 * <li>config.modules.magkit-tools-edit.config.moveConfirmWorkspaces.xml</li>
 * <li>config.modules.magkit-tools-edit.config.statusBarConfig.xml</li>
 * </ul>
 *
 * @author Oliver Emke
 * @since 2014-03-31
 */
public class EditToolsVersionHandler extends BootstrapModuleVersionHandler {

    private final Task _statusBarConfig = new BootstrapConditionally("Add move workspace config", "/mgnl-bootstrap/install/magkit-tools-edit/config.modules.magkit-tools-edit.config.moveConfirmWorkspaces.xml");
    private final Task _workspaceMoveConfig = new BootstrapConditionally("Add status bar config", "/mgnl-bootstrap/install/magkit-tools-edit/config.modules.magkit-tools-edit.config.statusBarConfig.xml");

    /**
     * Returns the default update tasks for a specific module version.
     * Adds conditional bootstrap tasks for status bar and move workspace configurations.
     *
     * @param forVersion the version for which to get update tasks
     * @return the list of update tasks
     */
    @Override
    protected List<Task> getDefaultUpdateTasks(final Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);

        // conditionally install bootstrap tasks
        tasks.add(_statusBarConfig);
        tasks.add(_workspaceMoveConfig);

        return tasks;
    }

}
