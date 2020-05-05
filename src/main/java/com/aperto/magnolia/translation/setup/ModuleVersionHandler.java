package com.aperto.magnolia.translation.setup;

import info.magnolia.jcr.nodebuilder.task.ErrorHandling;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeTypes.ContentNode;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * Module version handler for this module.
 *
 * @author frank.sommer
 * @since 24.11.2017
 */
public class ModuleVersionHandler extends DefaultModuleVersionHandler {

    public ModuleVersionHandler() {
        DeltaBuilder update110 = DeltaBuilder.update("1.1.0", "Update to version 1.1.0.");
        update110.addTask(new NodeExistsDelegateTask("Remove apps in JCR", "/modules/magnolia-translation/apps", new RemoveNodeTask("Remove apps", "/modules/magnolia-translation/apps")));
        register(update110);

        DeltaBuilder update141 = DeltaBuilder.update("1.4.1", "Update to version 1.4.1.");
        update141.addTask(new NodeExistsDelegateTask("Remove dialogs in JCR", "/modules/magnolia-translation/dialogs", new RemoveNodeTask("Remove dialogs", "/modules/magnolia-translation/dialogs")));
        register(update141);
    }

    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        List<Task> tasks = new ArrayList<>();
        tasks.add(new NodeBuilderTask("Register translation app", "Register translation app in admin central.", ErrorHandling.logging, CONFIG, "/modules/ui-admincentral/config/appLauncherLayout/groups/edit/apps", addNode("translation", ContentNode.NAME)));
        return tasks;
    }
}
