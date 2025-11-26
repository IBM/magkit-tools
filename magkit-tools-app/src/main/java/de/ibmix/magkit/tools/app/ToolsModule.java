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

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module lifecycle implementation for the magkit-tools-app module.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Manages module startup and shutdown lifecycle</li>
 *   <li>Logs module lifecycle events</li>
 * </ul>
 * This module provides various administrative tools and utilities for Magnolia CMS,
 * including JCR query execution and version pruning capabilities.
 *
 * @author frank.sommer
 * @since 2012-03-02
 */
public class ToolsModule implements ModuleLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsModule.class);

    /**
     * Initializes the module during Magnolia startup.
     *
     * @param moduleLifecycleContext the context providing access to module lifecycle information
     */
    @Override
    public void start(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Start extended module ...");
    }

    /**
     * Performs cleanup when the module is stopped during Magnolia shutdown.
     *
     * @param moduleLifecycleContext the context providing access to module lifecycle information
     */
    @Override
    public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Stop extended module ...");
    }
}
