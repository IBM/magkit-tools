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
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.getNode;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.removeIfExists;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static java.util.Arrays.asList;

/**
 * Version Handler for the edit tools.
 *
 * @author Oliver Emke
 * @since 31.03.14
 */
public class EditToolsVersionHandler extends BootstrapModuleVersionHandler {
    private static final String MODULE_PAGES = "pages";
    private static final String MODULE_DAM = "dam-app";
    private static final String SECTION_AREA_ACTIONS = "areaActions";
    private static final String SECTION_COMPONENT_ACTIONS = "componentActions";
    private static final String SECTION_PAGE_ACTIONS = "pageActions";
    private static final String SECTION_PAGE_NODE_AREA_ACTIONS = "pageNodeAreaActions";
    private static final String ACTION_EDIT_PAGE_PROPERTIES = "editProperties";
    private static final String ACTION_JUMP_CMP = "jumpToBrowser";
    private static final String ACTION_PREVIEW_EXTERNAL_CMP = "previewExternal";


    private final Task _workspaceMoveConfig = new BootstrapConditionally("Add move workspace config", "/mgnl-bootstrap/install/magkit-tools-edit/config.modules.magkit-tools-edit.config.moveConfirmWorkspaces.xml");

    public EditToolsVersionHandler() {
        DeltaBuilder update140 = DeltaBuilder.update("1.4.0", "Update to version 1.4.0.");
        Task removeJcrPagesAppConfig = selectModuleConfig("Remove jcr configs", "Remove jcr configs in pages app.", MODULE_PAGES,
            getNode("apps/pages/subApps/browser/actions").then(
                removeIfExists(ACTION_PREVIEW_EXTERNAL_CMP),
                removeIfExists("viewInJcr")
            ),
            getNode("apps/pages/subApps/browser/actionbar/sections/pageActions/groups/editingActions/items").then(
                removeIfExists(ACTION_PREVIEW_EXTERNAL_CMP),
                removeIfExists(ACTION_EDIT_PAGE_PROPERTIES)
            ),
            getNode("apps/pages/subApps/detail/actions").then(
                removeIfExists(ACTION_JUMP_CMP),
                removeIfExists("viewInJcr")
            ),
            getNode("apps/pages/subApps/detail/actionbar/sections").then(
                ArrayUtils.addAll(
                    addToSections(
                        getNode("groups/editingFlow/items").then(
                            removeIfExists(ACTION_JUMP_CMP)
                        )
                    ),
                    addToSections(
                        getNode("groups/editingActions/items").then(
                            removeIfExists(ACTION_EDIT_PAGE_PROPERTIES)
                        )
                    )
                )
            ),
            getNode("apps/pages/subApps/browser/workbench/contentViews/list/columns").then(
                removeIfExists("lastModUser"),
                removeIfExists("createdByUser")
            )
        );
        Task removeJcrAssetsAppConfig = selectModuleConfig("Remove jcr configs", "Remove jcr configs in assets app.", MODULE_DAM,
            getNode("apps/assets/subApps/browser/workbench/contentViews/list/columns").then(
                removeIfExists("lastModUser"),
                removeIfExists("createdByUser")
            )
        );
        update140.addTasks(asList(
            selectModuleConfig("Remove virtual uri mapping", "Remove virtual uri mapping config.", "magkit-tools-edit", removeIfExists("virtualURIMapping")),
            removeJcrPagesAppConfig,
            removeJcrAssetsAppConfig
        ));
        register(update140);
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(final Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);

        // conditionally install bootstrap tasks
        tasks.add(_workspaceMoveConfig);

        return tasks;
    }

    private NodeOperation[] addToSections(final NodeOperation nodeOperation) {
        return new NodeOperation[]{
            addOrGetContentNode(SECTION_PAGE_ACTIONS).then(nodeOperation),
            addOrGetContentNode(SECTION_AREA_ACTIONS).then(nodeOperation),
            addOrGetContentNode(SECTION_PAGE_NODE_AREA_ACTIONS).then(nodeOperation),
            addOrGetContentNode(SECTION_COMPONENT_ACTIONS).then(nodeOperation)
        };
    }
}
