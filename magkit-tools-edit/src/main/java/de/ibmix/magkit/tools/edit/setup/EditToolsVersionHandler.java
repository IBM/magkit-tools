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

import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.getNode;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.removeIfExists;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;

/**
 * Version Handler for the edit tools.
 *
 * @author Oliver Emke
 * @since 31.03.14
 */
public class EditToolsVersionHandler extends BootstrapModuleVersionHandler {

    private static final String MODULE_DAM = "dam-app";

    private final Task _workspaceMoveConfig = new BootstrapConditionally("Add move workspace config", "/mgnl-bootstrap/install/magkit-tools-edit/config.modules.magkit-tools-edit.config.moveConfirmWorkspaces.xml");

    public EditToolsVersionHandler() {
        Task removeJcrAssetsAppConfig = selectModuleConfig("Remove jcr configs", "Remove jcr configs in assets app.", MODULE_DAM,
            getNode("apps/assets/subApps/browser/workbench/contentViews/list/columns").then(
                removeIfExists("lastModUser"),
                removeIfExists("createdByUser")
            )
        );
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(final Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);

        // conditionally install bootstrap tasks
        tasks.add(_workspaceMoveConfig);

        return tasks;
    }

}
