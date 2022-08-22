package com.aperto.magnolia.translation.setup;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
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

import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
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
