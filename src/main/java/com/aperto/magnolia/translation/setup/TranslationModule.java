package com.aperto.magnolia.translation.setup;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translation module.
 * @author Aperto
 */
public class TranslationModule implements ModuleLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationModule.class);

    private String _basePath;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Start translation module ...");
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Stop translation module ...");
    }

    public String getBasePath() {
        return _basePath;
    }

    public void setBasePath(String basePath) {
        _basePath = basePath;
    }
}