package com.aperto.magnolia.translation.setup;

import com.aperto.magkit.module.BootstrapModuleVersionHandler;
import com.aperto.magkit.module.delta.StandardTasks;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import java.util.List;

/**
 * Module version handler for this module.
 *
 * @author frank.sommer
 * @since 24.11.2017
 */
public class ModuleVersionHandler extends BootstrapModuleVersionHandler {
    @Override
    protected List<Task> getExtraInstallTasks(final InstallContext installContext) {
        List<Task> tasks = super.getExtraInstallTasks(installContext);
        tasks.add(StandardTasks.addAppsToLauncher("edit", "", true, "translation"));
        return tasks;
    }
}
