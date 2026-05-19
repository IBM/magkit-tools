package de.ibmix.magkit.tools.app.setup;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools App
 * %%
 * Copyright (C) 2023 - 2026 IBM iX
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

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;

/**
 * Module version handler for the app module.
 *
 * @author frank.sommer
 * @since 19.05.2026
 */
public class AppModuleVersionHandler extends DefaultModuleVersionHandler {
    public AppModuleVersionHandler() {
        final DeltaBuilder update120 = DeltaBuilder.update("1.2.0", "Update tasks for Version 1.2.0");
        update120.addTask(new RemoveNodeTask("Remove field types", "/modules/magkit-tools-app/fieldTypes"));
        register(update120);
    }
}
