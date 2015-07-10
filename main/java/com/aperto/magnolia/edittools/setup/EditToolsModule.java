package com.aperto.magnolia.edittools.setup;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Edit tools module class.
 *
 * @author frank.sommer
 * @since 09.07.2015
 */
public class EditToolsModule implements ModuleLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditToolsModule.class);

    private PublicLinkConfig _publicLinkConfig;

    @Override
    public void start(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Start editor-tools-module ...");
    }

    @Override
    public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Stop editor-tools-module ...");
    }

    public PublicLinkConfig getPublicLinkConfig() {
        return _publicLinkConfig;
    }

    public void setPublicLinkConfig(final PublicLinkConfig publicLinkConfig) {
        _publicLinkConfig = publicLinkConfig;
    }
}
