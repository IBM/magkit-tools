package com.aperto.magnolia.edittools.setup;

import com.aperto.magkit.module.BootstrapModuleVersionHandler;
import com.aperto.magnolia.edittools.action.EditPageActionDefinition;
import com.aperto.magnolia.edittools.action.OpenPagePropertiesAction;
import com.aperto.magnolia.edittools.action.OpenPreviewNewWindowAction;
import com.aperto.magnolia.edittools.action.OpenTreeOnCurrentPositionAction;
import com.aperto.magnolia.edittools.rule.IsElementEditableRule;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.pages.app.action.DuplicatePageComponentActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.framework.action.OpenEditDialogActionDefinition;
import info.magnolia.ui.framework.availability.IsNotDeletedRule;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import java.util.List;

import static com.aperto.magkit.module.delta.StandardTasks.PN_CLASS;
import static com.aperto.magkit.module.delta.StandardTasks.PN_EXTENDS;
import static com.aperto.magkit.module.delta.StandardTasks.PN_ICON;
import static com.aperto.magkit.module.delta.StandardTasks.PN_IMPL_CLASS;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.getNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.orderBefore;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.removeIfExists;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.setProperty;
import static com.aperto.magkit.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static info.magnolia.jcr.util.NodeTypes.Created.CREATED_BY;
import static info.magnolia.jcr.util.NodeTypes.LastModified.LAST_MODIFIED_BY;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Version Handler for the edit tools.
 *
 * @author Oliver Emke
 * @since 31.03.14
 */
public class EditToolsVersionHandler extends BootstrapModuleVersionHandler {
    private static final String CN_GROUPS = "groups";
    private static final String CN_ITEMS = "items";
    private static final String ACTION_PREVIEW_EXTERNAL_CMP = "previewExternal";
    private static final String SECTION_EDITING_ACTIONS = "editingActions";
    private static final String SECTION_COMPONENT_ACTIONS = "componentActions";
    private static final String SECTION_PAGE_ACTIONS = "pageActions";
    private static final String EDITING_FLOW = "editingFlow";

    private static final String MODULE_PAGES = "pages";
    private static final String MODULE_DAM = "dam-app";
    private static final String PN_PROPERTY_NAME = "propertyName";
    private static final String PN_DISPLAY_IN_CHOOSE_DIALOG = "displayInChooseDialog";
    private static final String PN_SORTABLE = "sortable";
    private static final String PN_WIDTH = "width";
    private static final String PN_LABEL = "label";
    private static final String PN_AVAILABILITY = "availability";
    private static final String SECTION_AREA_ACTIONS = "areaActions";
    private static final String ACTION_EDIT_PAGE_PROPERTIES = "editProperties";
    private static final String SECTION_PAGE_NODE_AREA_ACTIONS = "pageNodeAreaActions";
    private static final String ACTION_JUMP_CMP = "jumpToBrowser";
    private static final String NN_ACTIONS = "actions";

    private final Task _addExternalPreviewActionToBrowserSubApp = selectModuleConfig("Add external preview action", "Add external preview action in pages app.", MODULE_PAGES,
        getNode("apps/pages/subApps").then(
            getNode("browser").then(
                getNode(NN_ACTIONS).then(
                    addOrGetContentNode(ACTION_PREVIEW_EXTERNAL_CMP).then(
                        addOrGetContentNode(PN_AVAILABILITY + "/rules/isNotDeleted").then(
                            addOrSetProperty(PN_IMPL_CLASS, IsNotDeletedRule.class.getName())
                        ),
                        addOrSetProperty(PN_ICON, "icon-view"),
                        addOrSetProperty(PN_LABEL, "previewExternal.label"),
                        addOrSetProperty(PN_CLASS, ConfiguredActionDefinition.class.getName()),
                        addOrSetProperty(PN_IMPL_CLASS, OpenPreviewNewWindowAction.class.getName()))
                ),
                getNode("actionbar/sections").then(
                    addOrGetContentNode(SECTION_PAGE_ACTIONS).then(
                        addOrGetContentNode(CN_GROUPS).then(
                            addOrGetContentNode(SECTION_EDITING_ACTIONS).then(
                                addOrGetContentNode(CN_ITEMS).then(
                                    addOrGetContentNode(ACTION_PREVIEW_EXTERNAL_CMP).then(
                                        orderBefore(ACTION_PREVIEW_EXTERNAL_CMP, "edit")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    );

    private final Task _addJumpToBrowserAction = selectModuleConfig("Add To Browser action", "Add to browser jump action in pages app.", MODULE_PAGES,
        getNode("apps/pages/subApps").then(
            getNode("detail").then(
                getNode(NN_ACTIONS).then(
                    addOrGetContentNode(ACTION_JUMP_CMP).then(
                        addOrSetProperty(PN_ICON, "icon-view-tree"),
                        addOrSetProperty(PN_LABEL, "jumpToBrowser.label"),
                        addOrSetProperty(PN_CLASS, ConfiguredActionDefinition.class.getName()),
                        addOrSetProperty(PN_IMPL_CLASS, OpenTreeOnCurrentPositionAction.class.getName())
                    )
                ),
                getNode("actionbar/sections").then(
                    addToSections(addActionToEditingFlowGroup(ACTION_JUMP_CMP))
                )
            )
        )
    );

    private final Task _addEditPropertiesActionToBrowser = selectModuleConfig("Add page properties action", "Add page properties action to pages browser sub app.", MODULE_PAGES,
        getNode("apps/pages/subApps/browser").then(
            getNode(NN_ACTIONS).then(
                addOrGetContentNode(ACTION_EDIT_PAGE_PROPERTIES).then(
                    addOrGetContentNode("availability").then(
                        getNode("rules/IsNotDeletedRule").then(
                            addOrSetProperty(PN_IMPL_CLASS, IsNotDeletedRule.class.getName())
                        ),
                        addOrSetProperty("writePermissionRequired", Boolean.TRUE)
                    ),
                    addOrSetProperty(PN_CLASS, OpenEditDialogActionDefinition.class.getName()),
                    addOrSetProperty(PN_IMPL_CLASS, OpenPagePropertiesAction.class.getName()),
                    addOrSetProperty(PN_LABEL, "pages.detail.actions.editProperties.label"),
                    addOrSetProperty(PN_ICON, "icon-edit")
                )
            ),
            getNode("actionbar/sections/pageActions/groups/editingActions/items").then(
                addOrGetContentNode(ACTION_EDIT_PAGE_PROPERTIES).then(
                    orderBefore(ACTION_EDIT_PAGE_PROPERTIES, "editPageName")
                )
            )
        )
    );

    private NodeOperation[] addToSections(final NodeOperation nodeOperation) {
        return new NodeOperation[]{
            addOrGetContentNode(SECTION_PAGE_ACTIONS).then(nodeOperation),
            addOrGetContentNode(SECTION_AREA_ACTIONS).then(nodeOperation),
            addOrGetContentNode(SECTION_PAGE_NODE_AREA_ACTIONS).then(nodeOperation),
            addOrGetContentNode(SECTION_COMPONENT_ACTIONS).then(nodeOperation)
        };
    }

    private final Task _addLastModifiedAndCreatorToListViewOfPagesApp = selectModuleConfig("Add last modified and creator to list view.", "", MODULE_PAGES,
        getNode("apps/pages/subApps/browser/workbench/contentViews/list/columns").then(
            addColumn("lastModUser", LAST_MODIFIED_BY, "column.lastModUser.label"),
            addColumn("createdByUser", CREATED_BY, "column.createdByUser.label")
        )
    );

    private final Task _addLastModifiedAndCreatorToListViewOfDamApp = selectModuleConfig("Add last modified and creator to list view.", "", MODULE_DAM,
        getNode("apps/assets/subApps/browser/workbench/contentViews/list/columns").then(
            addColumn("lastModUser", LAST_MODIFIED_BY, "column.lastModUser.label"),
            addColumn("createdByUser", CREATED_BY, "column.createdByUser.label")
        )
    );

    private final Task _updateEditPagePropertyAction = selectModuleConfig("Add Edit PageProperties everywhere in detail page view", "", MODULE_PAGES,
        getNode("apps/pages/subApps/detail/").then(
            getNode("actions/editProperties").then(
                getNode("availability/rules/isPageEditable").then(
                    addOrSetProperty(PN_IMPL_CLASS, IsElementEditableRule.class.getName())
                ),
                addOrSetProperty(PN_CLASS, EditPageActionDefinition.class.getName()),
                addOrSetProperty(PN_ICON, "icon-edit"),
                removeIfExists(PN_EXTENDS)
            ),
            getNode("actionbar/sections").then(
                addOrGetContentNode(SECTION_PAGE_ACTIONS).then(
                    addOrGetContentNode(CN_GROUPS).then(
                        addOrGetContentNode(SECTION_EDITING_ACTIONS).then(
                            addOrGetContentNode(CN_ITEMS).then(
                                addOrGetContentNode(ACTION_EDIT_PAGE_PROPERTIES)
                            )
                        )
                    )
                ),
                addOrGetContentNode(SECTION_AREA_ACTIONS).then(
                    addOrGetContentNode(CN_GROUPS).then(
                        addOrGetContentNode(SECTION_EDITING_ACTIONS).then(
                            addOrGetContentNode(CN_ITEMS).then(
                                addOrGetContentNode(ACTION_EDIT_PAGE_PROPERTIES).then(
                                    orderBefore(ACTION_EDIT_PAGE_PROPERTIES, "editArea")
                                )
                            )
                        )
                    )
                ),
                addOrGetContentNode(SECTION_PAGE_NODE_AREA_ACTIONS).then(
                    addOrGetContentNode(CN_GROUPS).then(
                        addOrGetContentNode(SECTION_EDITING_ACTIONS).then(
                            addOrGetContentNode(CN_ITEMS).then(
                                addOrGetContentNode(ACTION_EDIT_PAGE_PROPERTIES).then(
                                    orderBefore(ACTION_EDIT_PAGE_PROPERTIES, "editPageNodeArea")
                                )
                            )
                        )
                    )
                ),
                addOrGetContentNode(SECTION_COMPONENT_ACTIONS).then(
                    addOrGetContentNode(CN_GROUPS).then(
                        addOrGetContentNode(SECTION_EDITING_ACTIONS).then(
                            addOrGetContentNode(CN_ITEMS).then(
                                addOrGetContentNode(ACTION_EDIT_PAGE_PROPERTIES).then(
                                    orderBefore(ACTION_EDIT_PAGE_PROPERTIES, "editComponent")
                                )
                            )
                        )
                    )
                )
            )
        )
    );

    private NodeOperation addActionToEditingFlowGroup(String actionName) {
        return addOrGetContentNode(CN_GROUPS).then(
            addOrGetContentNode(EDITING_FLOW).then(
                addOrGetContentNode(CN_ITEMS).then(
                    addOrGetContentNode(actionName)
                )
            )
        );
    }

    private NodeOperation removeActionToEditingFlowGroup(String actionName) {
        return addOrGetContentNode(CN_GROUPS).then(
            addOrGetContentNode(EDITING_FLOW).then(
                addOrGetContentNode(CN_ITEMS).then(
                    removeIfExists(actionName)
                )
            )
        );
    }

    private NodeOperation addColumn(final String columnNodeName, final String columnPropertyName, final String label) {
        return addOrGetContentNode(columnNodeName).then(
            addOrSetProperty(PN_CLASS, MetaDataColumnDefinition.class.getName()),
            addOrSetProperty(PN_DISPLAY_IN_CHOOSE_DIALOG, false),
            addOrSetProperty(PN_PROPERTY_NAME, columnPropertyName),
            addOrSetProperty(PN_SORTABLE, "true"),
            addOrSetProperty(PN_LABEL, label),
            addOrSetProperty(PN_WIDTH, 80L)
        );
    }

    private final Task _logoUriMapping = new BootstrapConditionally("Add virtual uri mapping for logo", "/mgnl-bootstrap/install/magnolia-editor-tools/config.modules.magnolia-editor-tools.virtualURIMapping.logo.xml");

    private final Task _workspaceMoveConfig = new BootstrapConditionally("Add move workspace config", "/mgnl-bootstrap/install/magnolia-editor-tools/config.modules.magnolia-editor-tools.config.moveConfirmWorkspaces.xml");

    public EditToolsVersionHandler() {
        DeltaBuilder update110 = DeltaBuilder.update("1.1.0", "Update to version 1.1.0.");
        update110.addTasks(asList(new Task[]{
            selectModuleConfig("Reset pages app", "Reset duplicate component action in pages app,", MODULE_PAGES,
                getNode("apps/pages/subApps/detail/actions/duplicateComponent").then(
                    setProperty(PN_CLASS, DuplicatePageComponentActionDefinition.class.getName()),
                    removeIfExists(PN_IMPL_CLASS)
                )
            ),
            selectModuleConfig("Remove link service", "Remove link service from context attributes.", "rendering",
                getNode("renderers/freemarker/contextAttributes").then(
                    removeIfExists("etls")
                )
            )
        }));
        register(update110);

        DeltaBuilder update120 = DeltaBuilder.update("1.2.0", "Update to version 1.2.0.");
        final Task removeCustomCopyPasteActions = selectModuleConfig("Remove copy action from pages app", EMPTY, MODULE_PAGES,
            getNode("apps/pages/subApps/detail").then(
                getNode(NN_ACTIONS).then(
                    removeIfExists("copyNode"),
                    removeIfExists("pasteNode")
                ),
                getNode("actionbar/sections/areaActions/groups/addingActions/items").then(
                    removeIfExists("pasteNode")
                ),
                getNode("actionbar/sections/componentActions/groups/editingActions/items").then(
                    removeIfExists("copyNode")
                )
            )
        );
        final Task removeExternalPreviewActionFromDetailSubApp = selectModuleConfig("Remove external preview action", "Remove external preview action from pages app.", MODULE_PAGES,
            getNode("apps/pages/subApps").then(
                getNode("detail").then(
                    getNode(NN_ACTIONS).then(
                        removeIfExists(ACTION_PREVIEW_EXTERNAL_CMP)
                    ),
                    getNode("actionbar/sections").then(
                        addToSections(removeActionToEditingFlowGroup(ACTION_PREVIEW_EXTERNAL_CMP))
                    )
                )
            )
        );
        update120.addTasks(asList(removeCustomCopyPasteActions, removeExternalPreviewActionFromDetailSubApp));
        register(update120);
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(final Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);
        tasks.add(_addExternalPreviewActionToBrowserSubApp);
        tasks.add(_addLastModifiedAndCreatorToListViewOfPagesApp);
        tasks.add(_addLastModifiedAndCreatorToListViewOfDamApp);
        tasks.add(_updateEditPagePropertyAction);
        tasks.add(_addJumpToBrowserAction);
        tasks.add(_logoUriMapping);
        tasks.add(_workspaceMoveConfig);
        tasks.add(_addEditPropertiesActionToBrowser);
        return tasks;
    }

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        List<Task> tasks = super.getExtraInstallTasks(installContext);
        tasks.add(_addExternalPreviewActionToBrowserSubApp);
        tasks.add(_addLastModifiedAndCreatorToListViewOfPagesApp);
        tasks.add(_addLastModifiedAndCreatorToListViewOfDamApp);
        tasks.add(_updateEditPagePropertyAction);
        tasks.add(_addJumpToBrowserAction);
        tasks.add(_addEditPropertiesActionToBrowser);
        return tasks;
    }
}
