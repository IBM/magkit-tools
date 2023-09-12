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

import de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.module.delta.RemoveNodeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.util.NodeTypes.Activatable.LAST_ACTIVATED_VERSION;
import static info.magnolia.jcr.util.NodeTypes.Activatable.LAST_ACTIVATED_VERSION_CREATED;
import static info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible;

/**
 * Module version handler for this module.
 *
 * @author frank.sommer
 * @since 24.11.2017
 */
public class ModuleVersionHandler extends DefaultModuleVersionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleVersionHandler.class);

    public ModuleVersionHandler() {
        DeltaBuilder update110 = DeltaBuilder.update("1.1.0", "Update to version 1.1.0.");
        update110.addTask(new NodeExistsDelegateTask("Remove apps in JCR", "/modules/magnolia-translation/apps", new RemoveNodeTask("Remove apps", "/modules/magnolia-translation/apps")));
        register(update110);

        DeltaBuilder update141 = DeltaBuilder.update("1.4.1", "Update to version 1.4.1.");
        update141.addTask(new NodeExistsDelegateTask("Remove dialogs in JCR", "/modules/magnolia-translation/dialogs", new RemoveNodeTask("Remove dialogs", "/modules/magnolia-translation/dialogs")));
        register(update141);

        DeltaBuilder update151 = DeltaBuilder.update("1.5.1", "Update to version 1.5.1.");
        update151.addTask(new QueryTask("Remove version metadata", "Remove version metadata in translation nodes.", WS_TRANSLATION, "select * from [" + Translation.NAME + "]") {
            @Override
            protected void operateOnNode(InstallContext installContext, Node node) {
                try {
                    if (node.hasProperty(LAST_ACTIVATED_VERSION)) {
                        node.getProperty(LAST_ACTIVATED_VERSION).remove();
                    }
                    if (node.hasProperty(LAST_ACTIVATED_VERSION_CREATED)) {
                        node.getProperty(LAST_ACTIVATED_VERSION_CREATED).remove();
                    }
                } catch (RepositoryException e) {
                    LOGGER.error("Error removing version metadata for {}.", getNodePathIfPossible(node), e);
                }
            }
        });
        register(update151);
    }
}
