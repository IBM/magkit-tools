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

import de.ibmix.magkit.setup.BootstrapModuleVersionHandler;
import de.ibmix.magkit.tools.app.field.AclFieldDefinition;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;

import java.util.List;

import static de.ibmix.magkit.setup.delta.StandardTasks.PN_CLASS;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrGetContentNode;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static de.ibmix.magkit.setup.nodebuilder.NodeOperationFactory.getNode;
import static de.ibmix.magkit.setup.nodebuilder.task.NodeBuilderTaskFactory.selectModuleConfig;
import static org.apache.jackrabbit.JcrConstants.JCR_UUID;

/**
 * Bootstrap on update handler.
 *
 * @author frank.sommer
 * @since 12.11.12
 */
public class ToolsModuleVersionHandler extends BootstrapModuleVersionHandler {

    private static final String PN_LABEL = "label";
    private static final String PN_NAME = "name";
    private static final String PN_READONLY = "readOnly";
    private static final String PN_EXTENDS = "extends";

    private static final String MODULE_PAGES = "pages";
    private static final String MODULE_SECURITY_APP = "security-app";

    private final Task _addIdentifierToEditPageDialog = selectModuleConfig("Add identifier to editPage dialog.", "Add identifier to editPage dialog.", MODULE_PAGES,
        getNode("dialogs/editPage/form/tabs/tabPage/fields").then(
            addOrGetContentNode("identifier").then(
                addOrSetProperty(PN_CLASS, TextFieldDefinition.class.getName()),
                addOrSetProperty(PN_LABEL, "paged.editPage.identifier"),
                addOrSetProperty(PN_NAME, JCR_UUID),
                addOrSetProperty(PN_READONLY, "true")
            )
        )
    );

    private final Task _addAclTab = selectModuleConfig("Add acl overview tab to user and group dialog.", "Add acl overview tab to user and group dialog.", MODULE_SECURITY_APP,
        addOrGetContentNode("dialogs/user/form/tabs/aclOverview").then(
            addOrSetProperty(PN_LABEL, "ACL Overview"),
            addOrGetContentNode("fields").then(
                addOrGetContentNode("overview").then(
                    addOrSetProperty(PN_CLASS, AclFieldDefinition.class.getName()),
                    addOrSetProperty(PN_LABEL, "Permissions")
                )
            )
        ),
        addOrGetContentNode("dialogs/group/form/tabs/aclOverview").then(
            addOrSetProperty(PN_EXTENDS, "/modules/security-app/dialogs/user/form/tabs/aclOverview")
        )
    );

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = super.getExtraInstallTasks(installContext);
        tasks.add(_addIdentifierToEditPageDialog);
        tasks.add(_addAclTab);
        return tasks;
    }

    @Override
    protected List<Task> getDefaultUpdateTasks(Version forVersion) {
        List<Task> tasks = super.getDefaultUpdateTasks(forVersion);
        tasks.add(_addIdentifierToEditPageDialog);
        tasks.add(_addAclTab);
        return tasks;
    }
}
