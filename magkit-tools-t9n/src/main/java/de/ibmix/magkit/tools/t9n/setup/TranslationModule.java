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
 * Translation module.
 *
 * @author IBM iX
 */
@Slf4j
public class TranslationModule implements ModuleLifecycle {

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
