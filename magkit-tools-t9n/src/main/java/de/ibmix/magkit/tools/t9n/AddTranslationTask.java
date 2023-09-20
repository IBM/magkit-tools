package de.ibmix.magkit.tools.t9n;

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
import info.magnolia.jcr.nodebuilder.NodeOperation;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

import static de.ibmix.magkit.tools.t9n.MagnoliaTranslationServiceImpl.BASE_QUERY;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PN_KEY;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
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
 * @author diana.racho (IBM iX)
 */
@SuppressWarnings("unused")
@Slf4j
public class AddTranslationTask extends AbstractTask {

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
