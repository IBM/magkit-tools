package com.aperto.magnolia.translation;

import com.aperto.magkit.nodebuilder.task.NodeBuilderTask;
import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrGetNode;
import static com.aperto.magkit.nodebuilder.NodeOperationFactory.addOrSetProperty;
import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.cms.core.Path.getValidatedLabel;
import static info.magnolia.jcr.nodebuilder.task.ErrorHandling.logging;

/**
 * Add all not existing keys from a properties file to translation workspace.
 *
 * @author diana.racho (Aperto AG)
 */
public class AddTranslationTask extends AbstractTask {
    private final String _baseName;
    private final Locale _locale;

    /**
     * Constructor for messages bundle registration.
     *
     * @param taskName task name
     * @param taskDescription task description
     * @param baseName bundle base name
     * @param locale locale
     */
    public AddTranslationTask(String taskName, String taskDescription, String baseName, Locale locale) {
        super(taskName, taskDescription);
        _baseName = baseName;
        _locale = locale;
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        ArrayDelegateTask task = new ArrayDelegateTask("Add translation key tasks");
        ResourceBundle bundle = ResourceBundle.getBundle(_baseName, _locale);
        for (String key : bundle.keySet()) {
            String keyNodeName = getValidatedLabel(key);
            task.addTask(
                new NodeExistsDelegateTask("Check translation key", "Check translation key", WS_TRANSLATION, "/" + keyNodeName, null,
                    new NodeBuilderTask("Create translation node", "", logging, WS_TRANSLATION,
                        addOrGetNode(keyNodeName, Translation.NAME).then(
                            addOrSetProperty(Translation.PN_KEY, key),
                            addOrSetProperty(Translation.PREFIX_NAME + _locale.getLanguage(), bundle.getString(key))
                        )
                    )
                )
            );
        }
        task.execute(installContext);
    }
}