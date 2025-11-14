package de.ibmix.magkit.tools.t9n.setup;

/*-
 * #%L
 * magkit-tools-t9n
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
import lombok.extern.slf4j.Slf4j;

/**
 * Module configuration and lifecycle management for the translation tools.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * Provides configuration options and lifecycle hooks for the translation module,
 * particularly the ability to configure a custom base path for organizing translations
 * within the translation workspace.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Configurable base path for translation organization</li>
 * <li>Module lifecycle management (start/stop hooks)</li>
 * <li>Integration with Magnolia's module system</li>
 * </ul>
 * <p>
 * <p><strong>Configuration:</strong></p>
 * The base path can be configured in the module descriptor to organize translations
 * hierarchically within the translation workspace.
 *
 * @author IBM iX
 * @since 2023-01-01
 */
@Slf4j
public class TranslationModule implements ModuleLifecycle {

    private String _basePath;

    /**
     * Called when the module is started during Magnolia initialization.
     *
     * @param moduleLifecycleContext the lifecycle context
     */
    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Start translation module ...");
    }

    /**
     * Called when the module is stopped during Magnolia shutdown.
     *
     * @param moduleLifecycleContext the lifecycle context
     */
    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Stop translation module ...");
    }

    /**
     * Returns the configured base path for translations in the workspace.
     *
     * @return the base path, or null if not configured
     */
    public String getBasePath() {
        return _basePath;
    }

    /**
     * Sets the base path for organizing translations within the workspace.
     *
     * @param basePath the base path (e.g., "/myproject")
     */
    public void setBasePath(String basePath) {
        _basePath = basePath;
    }
}
