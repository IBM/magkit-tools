package com.aperto.magnolia.translation;

import com.aperto.magkit.nodebuilder.task.NodeBuilderTask;
import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.cms.core.Path.getValidatedLabel;
import static info.magnolia.jcr.nodebuilder.task.ErrorHandling.logging;
import static java.util.Locale.ENGLISH;

/**
 * Add all not existing keys from a properties file to translation workspace.
 *
 * @author diana.racho (Aperto AG)
 */
public class AddTranslationTask extends AbstractTask {
    private static final String ND_TRANSLATION = "translation";

    private String _fileName;
    private Locale _locale = ENGLISH;

    public AddTranslationTask(String taskName, String taskDescription, String fileName, Locale locale) {
        super(taskName, taskDescription);
        _fileName = fileName;
        _locale = locale;
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        ArrayDelegateTask task = new ArrayDelegateTask("Add translation key tasks");
        ResourceBundle bundle = ResourceBundle.getBundle(_fileName, _locale);
        for (String key : Collections.list(bundle.getKeys())) {
            String keyNodeName = getValidatedLabel(key);
            task.addTask(
                new NodeExistsDelegateTask("Check translation key", "Check translation key", WS_TRANSLATION, "/" + ND_TRANSLATION + "/" + keyNodeName, null,
                    new NodeBuilderTask("Create translation node", "", logging, WS_TRANSLATION,
                        addOrGetNode(ND_TRANSLATION, NodeTypes.Folder.NAME).then(
                            addOrGetNode(keyNodeName, Translation.NAME).then(
                                addOrSetProperty(Translation.PN_KEY, key),
                                addOrSetProperty(Translation.PREFIX_NAME + _locale.getLanguage(), bundle.getString(key))
                            )
                        )
                    )
                )
            );
        }
        task.execute(installContext);
    }
}