package com.aperto.magnolia.translation;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.jcr.nodebuilder.Ops.addProperty;
import static info.magnolia.jcr.nodebuilder.task.ErrorHandling.logging;

/**
 * Add all not existing keys from a properties file to translation workspace.
 *
 * @author diana.racho (Aperto AG)
 */
@SuppressWarnings("unused")
public class AddTranslationTask extends AbstractTask {

    private final String _baseName;
    private final Locale _locale;
    private final String _basePath;

    /**
     * Constructor for messages bundle registration.
     *
     * @param taskName        task name
     * @param taskDescription task description
     * @param baseName        bundle base name
     * @param locale          locale
     */
    @SuppressWarnings("unused")
    public AddTranslationTask(String taskName, String taskDescription, String baseName, Locale locale) {
        this(taskName, taskDescription, baseName, locale, EMPTY);
    }

    /**
     * Constructor for messages bundle registration.
     *
     * @param taskName        task name
     * @param taskDescription task description
     * @param baseName        bundle base name
     * @param locale          locale
     * @param basePath        base path, may be null or empty or start with "/"
     */
    @SuppressWarnings("unused")
    public AddTranslationTask(String taskName, String taskDescription, String baseName, Locale locale, String basePath) {
        super(taskName, taskDescription);
        _baseName = baseName;
        _locale = locale;
        _basePath = defaultString(basePath) + "/";
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        ArrayDelegateTask task = new ArrayDelegateTask("Add translation key tasks");
        ResourceBundle bundle = ResourceBundle.getBundle(_baseName, _locale);
        NodeNameHelper nodeNameHelper = Components.getComponent(NodeNameHelper.class);

        for (String key : bundle.keySet()) {
            String keyNodeName = nodeNameHelper.getValidatedName(key);
            task.addTask(
                new NodeExistsDelegateTask("Check translation key", "Check translation key", WS_TRANSLATION, _basePath + keyNodeName, null,
                    new NodeBuilderTask("Create translation node", "", logging, WS_TRANSLATION,
                        addNode(keyNodeName, Translation.NAME).then(
                            addProperty(Translation.PN_KEY, (Object) key),
                            addProperty(Translation.PREFIX_NAME + _locale.getLanguage(), (Object) bundle.getString(key))
                        )
                    )
                )
            );
        }
        task.execute(installContext);
    }
}