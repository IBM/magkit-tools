package com.aperto.magnolia.translation;

import com.aperto.magnolia.translation.TranslationNodeTypes.Translation;
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.aperto.magnolia.translation.MagnoliaTranslationServiceImpl.BASE_QUERY;
import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PN_KEY;
import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;
import static com.aperto.magnolia.translation.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.cms.util.QueryUtil.search;
import static info.magnolia.jcr.nodebuilder.Ops.addNode;
import static info.magnolia.jcr.nodebuilder.Ops.addProperty;
import static info.magnolia.jcr.nodebuilder.Ops.getOrAddNode;
import static info.magnolia.jcr.nodebuilder.task.ErrorHandling.logging;
import static info.magnolia.jcr.util.NodeTypes.LastModified.LAST_MODIFIED;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(MagnoliaTranslationServiceImpl.class);

    private static final String ROOT_PATH = "/";

    private final String _baseName;
    private final Locale _locale;
    private final String _basePath;
    private final NodeNameHelper _nodeNameHelper;

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
    @SuppressWarnings({"unused", "WeakerAccess"})
    public AddTranslationTask(String taskName, String taskDescription, String baseName, Locale locale, String basePath) {
        super(taskName, taskDescription);
        _baseName = baseName;
        _locale = locale;
        _basePath = defaultString(basePath) + "/";
        _nodeNameHelper = Components.getComponent(NodeNameHelper.class);
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        ArrayDelegateTask task = new ArrayDelegateTask("Add translation key tasks");
        ResourceBundle bundle = ResourceBundle.getBundle(_baseName, _locale);
        Calendar now = Calendar.getInstance();

        for (String key : bundle.keySet()) {
            if (!keyExists(key)) {
                String keyNodeName = _nodeNameHelper.getValidatedName(key);
                task.addTask(new NodeBuilderTask("Create translation node", "", logging, WS_TRANSLATION, getOp(keyNodeName, key, bundle.getString(key), now)));
            }
        }
        task.execute(installContext);
    }

    private boolean keyExists(String key) {
        boolean keyExists = false;
        String statement = BASE_QUERY + '\'' + key + '\'';
        try {
            NodeIterator nodeIterator = search(WS_TRANSLATION, statement);
            keyExists = nodeIterator.hasNext();
        } catch (RepositoryException e) {
            LOGGER.error("Error on querying translation node for query {}.", statement, e);
        }
        return keyExists;
    }

    private NodeOperation getOp(String keyNodeName, String key, String value, final Calendar now) {
        NodeOperation addKeyNode = addNode(keyNodeName, Translation.NAME).then(
            addProperty(PN_KEY, (Object) key),
            addProperty(PREFIX_NAME + _locale.getLanguage(), (Object) value),
            addProperty(LAST_MODIFIED, now)
        );
        return ROOT_PATH.equals(_basePath) ? addKeyNode : getOrAddNode(removeStart(_basePath, ROOT_PATH), Translation.NAME).then(addKeyNode);
    }
}
