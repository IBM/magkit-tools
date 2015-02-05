package com.aperto.magnolia.edittools.setup;

import com.aperto.magkit.module.BootstrapModuleVersionHandler;
import com.aperto.magnolia.edittools.action.DuplicateComponentAction;
import com.aperto.magnolia.edittools.action.DuplicateComponentActionDefinition;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import java.util.List;

import static com.aperto.magkit.module.delta.StandardTasks.*;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.*;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static info.magnolia.jcr.nodebuilder.Ops.getNode;
import static info.magnolia.jcr.util.NodeTypes.Created.CREATED_BY;
import static info.magnolia.jcr.util.NodeTypes.LastModified.LAST_MODIFIED_BY;

/**
 * Version Handler for the edit tools.
 *
 * @author Oliver Emke
 * @since 31.03.14
 */
public class EditToolsVersionHandler extends BootstrapModuleVersionHandler {

    private static final String CN_GROUPS = "groups";
    private static final String CN_ITEMS = "items";
    private static final String CN_DEV_ACTIONS = "devActions";
    private static final String ACTION_DUPLICATE_CMP = "duplicateComponent";
    private static final String MODULE_PAGES = "pages";
    private static final String PN_PROPERTY_NAME = "propertyName";
    private static final String PN_DISPLAY_IN_CHOOSE_DIALOG = "displayInChooseDialog";
    private static final String PN_SORTABLE = "sortable";
    private static final String PN_WIDTH = "width";

    private final Task _registerDevActions = selectModuleConfig("Register developer actions", "Register developer actions", MODULE_PAGES,
        getNode("apps/pages/subApps/detail").then(
            getNode("actions").then(
                addOrGetContentNode(ACTION_DUPLICATE_CMP).then(
                    addOrSetProperty(PN_CLASS, DuplicateComponentActionDefinition.class.getName()),
                    addOrSetProperty(PN_ICON, "icon-duplicate"),
                    addOrSetProperty(PN_IMPL_CLASS, DuplicateComponentAction.class.getName())
                )
            ),
            getNode("actionbar/sections").then(
                addOrGetContentNode("componentActions").then(addOrGetContentNode(CN_GROUPS).then(
                        addOrGetContentNode("editingActions").then(
                            addOrGetContentNode(CN_ITEMS).then(
                                addOrGetContentNode(ACTION_DUPLICATE_CMP).then(
                                    orderBefore(ACTION_DUPLICATE_CMP, "startMoveComponent")
                            )
                        )
                    )
                )
                )
            )
        )
    );

    private final Task _addLastModifiedAndCreatorToListView = selectModuleConfig("Add last modified and creator to list view.", "", MODULE_PAGES,
        getNode("apps/pages/subApps/browser/workbench/contentViews/list/columns").then(
            addOrGetContentNode("lastModUser").then(
                addOrSetProperty(PN_CLASS, MetaDataColumnDefinition.class.getName()),
                addOrSetProperty(PN_DISPLAY_IN_CHOOSE_DIALOG, false),
                addOrSetProperty(PN_PROPERTY_NAME, LAST_MODIFIED_BY),
                addOrSetProperty(PN_SORTABLE, "true"),
                addOrSetProperty(PN_WIDTH, 80L)
            ),
            addOrGetContentNode("createdByUser").then(
                addOrSetProperty(PN_CLASS, MetaDataColumnDefinition.class.getName()),
                addOrSetProperty(PN_DISPLAY_IN_CHOOSE_DIALOG, false),
                addOrSetProperty(PN_PROPERTY_NAME, CREATED_BY),
                addOrSetProperty(PN_SORTABLE, "true"),
                addOrSetProperty(PN_WIDTH, 80L)
            )
        )
    );

    @Override
    protected List<Task> getDefaultUpdateTasks(final Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);
        tasks.add(_registerDevActions);
        return tasks;
    }

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        List<Task> tasks = super.getExtraInstallTasks(installContext);
        tasks.add(_registerDevActions);
        return tasks;
    }
}