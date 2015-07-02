package com.aperto.magnolia.edittools.setup;

import com.aperto.magkit.module.BootstrapModuleVersionHandler;
import com.aperto.magnolia.edittools.action.DuplicateComponentAction;
import com.aperto.magnolia.edittools.action.DuplicateComponentActionDefinition;
import com.aperto.magnolia.edittools.action.EditPageActionDefinition;
import com.aperto.magnolia.edittools.action.OpenPreviewNewWindowAction;
import com.aperto.magnolia.edittools.rule.IsElementEditableRule;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.framework.availability.IsNotDeletedRule;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import java.util.List;

import static com.aperto.magkit.module.delta.StandardTasks.PN_CLASS;
import static com.aperto.magkit.module.delta.StandardTasks.PN_EXTENDS;
import static com.aperto.magkit.module.delta.StandardTasks.PN_ICON;
import static com.aperto.magkit.module.delta.StandardTasks.PN_IMPL_CLASS;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.orderBefore;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.removeIfExists;
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
    private static final String ACTION_DUPLICATE_CMP = "duplicateComponent";
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
    public static final String ACTION_EDIT_PAGE_PROPERTIES = "editProperties";
    public static final String SECTION_PAGE_NODE_AREA_ACTIONS = "pageNodeAreaActions";

    private final Task _registerDevActions = selectModuleConfig("Register editor actions", "Register developer actions", MODULE_PAGES,
        getNode("apps/pages/subApps").then(
            getNode("detail").then(
                getNode("actions").then(
                    addOrGetContentNode(ACTION_DUPLICATE_CMP).then(
                        addOrSetProperty(PN_CLASS, DuplicateComponentActionDefinition.class.getName()),
                        addOrSetProperty(PN_ICON, "icon-duplicate"),
                        addOrSetProperty(PN_IMPL_CLASS, DuplicateComponentAction.class.getName())
                    ),
                    addOrGetNode(ACTION_PREVIEW_EXTERNAL_CMP, NodeTypes.ContentNode.NAME).then(
                        addOrGetNode(PN_AVAILABILITY, NodeTypes.ContentNode.NAME).then(
                            addOrGetNode("rules", NodeTypes.ContentNode.NAME).then(
                                addOrGetNode("isNotDeleted", NodeTypes.ContentNode.NAME).then(
                                    addOrSetProperty(PN_IMPL_CLASS, IsNotDeletedRule.class.getName())))),
                        addOrSetProperty(PN_ICON, "icon-view"),
                        addOrSetProperty(PN_LABEL, "previewExternal.label"),
                        addOrSetProperty(PN_CLASS, ConfiguredActionDefinition.class.getName()),
                        addOrSetProperty(PN_IMPL_CLASS, OpenPreviewNewWindowAction.class.getName()))
                ),
                getNode("actionbar/sections").then(
                    addOrGetContentNode(SECTION_COMPONENT_ACTIONS).then(
                        addOrGetContentNode(CN_GROUPS).then(
                            addOrGetContentNode(SECTION_EDITING_ACTIONS).then(
                                addOrGetContentNode(CN_ITEMS).then(
                                    addOrGetContentNode(ACTION_DUPLICATE_CMP).then(
                                        orderBefore(ACTION_DUPLICATE_CMP, "startMoveComponent")
                                    )
                                )
                            )
                        )
                    ),
                    addOrGetContentNode(SECTION_PAGE_ACTIONS).then(
                        addPreviewExternal()
                    ),
                    addOrGetContentNode("areaActions").then(
                        addPreviewExternal()
                    ),
                    addOrGetContentNode("pageNodeAreaActions").then(
                        addPreviewExternal()
                    ),
                    addOrGetContentNode(SECTION_COMPONENT_ACTIONS).then(
                        addPreviewExternal()
                    )
                )
            ),
            getNode("browser").then(
                getNode("actions").then(
                    addOrGetNode(ACTION_PREVIEW_EXTERNAL_CMP, NodeTypes.ContentNode.NAME).then(
                        addOrGetNode(PN_AVAILABILITY, NodeTypes.ContentNode.NAME).then(
                            addOrGetNode("rules", NodeTypes.ContentNode.NAME).then(
                                addOrGetNode("isNotDeleted", NodeTypes.ContentNode.NAME).then(
                                    addOrSetProperty(PN_IMPL_CLASS, IsNotDeletedRule.class.getName())))),
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
                                    addOrGetNode(ACTION_PREVIEW_EXTERNAL_CMP, NodeTypes.ContentNode.NAME).then(
                                        orderBefore(ACTION_PREVIEW_EXTERNAL_CMP, "edit"))
                                )
                            )
                        )
                    )
                )
            )
        )
    );

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
                                addOrGetNode(ACTION_EDIT_PAGE_PROPERTIES, NodeTypes.ContentNode.NAME)
                            )
                        )
                    )
                ),
                addOrGetContentNode(SECTION_AREA_ACTIONS).then(
                    addOrGetContentNode(CN_GROUPS).then(
                        addOrGetContentNode(SECTION_EDITING_ACTIONS).then(
                            addOrGetContentNode(CN_ITEMS).then(
                                addOrGetNode(ACTION_EDIT_PAGE_PROPERTIES, NodeTypes.ContentNode.NAME).then(
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
                                addOrGetNode(ACTION_EDIT_PAGE_PROPERTIES, NodeTypes.ContentNode.NAME).then(
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
                                addOrGetNode(ACTION_EDIT_PAGE_PROPERTIES, NodeTypes.ContentNode.NAME).then(
                                    orderBefore(ACTION_EDIT_PAGE_PROPERTIES, "editComponent")
                                )
                            )
                        )
                    )
                )
            )
        )
    );

    private NodeOperation addPreviewExternal() {
        return addOrGetContentNode(CN_GROUPS).then(
            addOrGetContentNode(EDITING_FLOW).then(
                addOrGetContentNode(CN_ITEMS).then(
                    addOrGetNode(ACTION_PREVIEW_EXTERNAL_CMP, NodeTypes.ContentNode.NAME).then(
                        orderBefore("preview", ACTION_PREVIEW_EXTERNAL_CMP)
                    )
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

    @Override
    protected List<Task> getDefaultUpdateTasks(final Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);
        tasks.add(_registerDevActions);
        tasks.add(_addLastModifiedAndCreatorToListViewOfPagesApp);
        tasks.add(_addLastModifiedAndCreatorToListViewOfDamApp);
        tasks.add(_updateEditPagePropertyAction);
        return tasks;
    }

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        List<Task> tasks = super.getExtraInstallTasks(installContext);
        tasks.add(_registerDevActions);
        tasks.add(_addLastModifiedAndCreatorToListViewOfPagesApp);
        tasks.add(_addLastModifiedAndCreatorToListViewOfDamApp);
        tasks.add(_updateEditPagePropertyAction);
        return tasks;
    }
}
