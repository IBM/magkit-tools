package com.aperto.magnolia.translation;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.cms.core.Path.getValidatedLabel;
import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.jcr.nodebuilder.Ops.addProperty;
import static info.magnolia.jcr.nodebuilder.Ops.getOrAddNode;
import static info.magnolia.jcr.nodebuilder.task.ErrorHandling.logging;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * Add all not existing keys from a properties file to translation workspace.
 *
 * @author diana.racho (Aperto AG)
 */
@SuppressWarnings("unused")
public class AddTranslationTask extends AbstractTask {
    private static final String ROOT_PATH = "/";

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
     * @param basePath        base path
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

        for (String key : bundle.keySet()) {
            String keyNodeName = getValidatedLabel(key);
            task.addTask(
                new NodeExistsDelegateTask("Check translation key", "Check translation key", WS_TRANSLATION, _basePath + keyNodeName, null,
                    new NodeBuilderTask("Create translation node", "", logging, WS_TRANSLATION,
                        getOp(keyNodeName, key, bundle.getString(key))
                    )
                )
            );
        }
        task.execute(installContext);
    }

    private NodeOperation getOp(String keyNodeName, String key, String value) {
        NodeOperation addKeyNode = addNode(keyNodeName, Translation.NAME).then(
            addProperty(Translation.PN_KEY, key),
            addProperty(Translation.PREFIX_NAME + _locale.getLanguage(), value)
        );
        return ROOT_PATH.equals(_basePath) ? addKeyNode : getOrAddNode(removeStart(_basePath, ROOT_PATH), Translation.NAME).then(addKeyNode);
    }
}