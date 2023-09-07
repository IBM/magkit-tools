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
 * Module class for this module.
 *
 * @author frank.sommer
 * @since 02.03.12
 */
public class ToolsModule implements ModuleLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsModule.class);

    @Override
    public void start(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Start extended module ...");
    }

    @Override
    public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
        LOGGER.info("Stop extended module ...");
    }
}
