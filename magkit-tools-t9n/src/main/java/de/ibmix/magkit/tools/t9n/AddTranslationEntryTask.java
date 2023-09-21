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
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.function.Consumer;

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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * Add all not existing keys from a properties file to translation workspace.
 *
 * @author diana.racho (IBM iX)
 */
@Slf4j
public class AddTranslationEntryTask extends AbstractTask {

    protected static final String ROOT_PATH = "/";

    private final String _baseName;
    private final Locale _locale;
    private final String _basePath;
    private final NodeNameHelper _nodeNameHelper;
    private final Calendar _now;

    /**
     * Constructor for messages bundle registration.
     *
     * @param taskName        task name
     * @param taskDescription task description
     * @param baseName        bundle base name
     * @param locale          locale
     */
    public AddTranslationEntryTask(String taskName, String taskDescription, String baseName, Locale locale) {
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
    public AddTranslationEntryTask(String taskName, String taskDescription, String baseName, Locale locale, String basePath) {
        super(taskName, taskDescription);
        _baseName = baseName;
        _locale = locale;
        _basePath = defaultString(basePath) + "/";

        _nodeNameHelper = Components.getComponent(NodeNameHelper.class);
        _now = Calendar.getInstance();
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        ArrayDelegateTask task = new ArrayDelegateTask("Add translation key tasks");
        addTranslationNodeTasks(task, installContext);
        task.execute(installContext);
    }

    protected void addTranslationNodeTasks(ArrayDelegateTask task, InstallContext installContext) {
        Resource resource = getResource(_locale.toString());
        addTasksForResource(resource, bundle -> {
            for (String key : bundle.keySet()) {
                if (!keyExists(key)) {
                    String keyNodeName = _nodeNameHelper.getValidatedName(key);
                    task.addTask(createTranslationEntryOperation(keyNodeName, key, bundle.getString(key)));
                }
            }
        });
    }

    protected void addTasksForResource(Resource resource, Consumer<ResourceBundle> bundleConsumer) {
        try (InputStream inputStream = resource.openStream()) {
            final PropertyResourceBundle bundle = new PropertyResourceBundle(new InputStreamReader(inputStream, UTF_8));
            bundleConsumer.accept(bundle);
        } catch (IOException e) {
            LOGGER.error("Error reading resource bundle.", e);
        }
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

    private Task createTranslationEntryOperation(String keyNodeName, String key, String value) {
        NodeOperation addKeyNode = addNode(keyNodeName, Translation.NAME).then(
            addProperty(PN_KEY, (Object) key),
            addProperty(PREFIX_NAME + _locale.getLanguage(), (Object) value),
            addProperty(LAST_MODIFIED, _now)
        );
        return new NodeBuilderTask("Create translation node", "", logging, WS_TRANSLATION, ROOT_PATH.equals(_basePath) ? addKeyNode : getOrAddNode(removeStart(_basePath, ROOT_PATH), Translation.NAME).then(addKeyNode));
    }

    /**
     * Loads the i18n resource.
     *
     * @param locale locale string
     * @return the resource
     */
    protected Resource getResource(String locale) {
        final ResourceOrigin resourceOrigins = Components.getComponent(ResourceOrigin.class);
        return resourceOrigins.getByPath("/" + _baseName.replace(".", "/") + "_" + locale + ".properties");
    }

    public NodeNameHelper getNodeNameHelper() {
        return _nodeNameHelper;
    }

    public String getBasePath() {
        return _basePath;
    }
}
