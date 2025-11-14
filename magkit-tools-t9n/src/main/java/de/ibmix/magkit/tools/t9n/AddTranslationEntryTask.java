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
 * Magnolia install task that imports translation keys from property files into the translation workspace.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * This task reads translation entries from resource bundles (properties files) and creates corresponding
 * nodes in the Magnolia translation workspace if they don't already exist.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Imports translation keys from properties files for specified locales</li>
 * <li>Checks for existing keys to avoid duplicates</li>
 * <li>Supports custom base paths for organizing translations</li>
 * <li>Automatically generates valid node names from translation keys</li>
 * <li>Sets last modified timestamps on created nodes</li>
 * </ul>
 * <p>
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * Task task = new AddTranslationEntryTask(
 *     "Import German translations",
 *     "Import translations from messages_de.properties",
 *     "com.example.messages",
 *     Locale.GERMAN
 * );
 * </pre>
 * <p>
 * <p><strong>Thread Safety:</strong></p>
 * This class is not thread-safe and should only be used within Magnolia's module installation context.
 *
 * @author diana.racho (IBM iX)
 * @since 2023-01-01
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
     * Creates a new task to import translation entries from a resource bundle.
     *
     * @param taskName        the display name of the task
     * @param taskDescription the detailed description of what the task does
     * @param baseName        the fully qualified base name of the resource bundle (e.g., "com.example.messages")
     * @param locale          the locale for which to import translations
     */
    public AddTranslationEntryTask(String taskName, String taskDescription, String baseName, Locale locale) {
        this(taskName, taskDescription, baseName, locale, EMPTY);
    }

    /**
     * Creates a new task to import translation entries from a resource bundle with a custom base path.
     *
     * @param taskName        the display name of the task
     * @param taskDescription the detailed description of what the task does
     * @param baseName        the fully qualified base name of the resource bundle (e.g., "com.example.messages")
     * @param locale          the locale for which to import translations
     * @param basePath        the base path in the translation workspace where nodes will be created
     */
    public AddTranslationEntryTask(String taskName, String taskDescription, String baseName, Locale locale, String basePath) {
        super(taskName, taskDescription);
        _baseName = baseName;
        _locale = locale;
        _basePath = defaultString(basePath) + "/";

        _nodeNameHelper = Components.getComponent(NodeNameHelper.class);
        _now = Calendar.getInstance();
    }

    /**
     * Executes the task by creating subtasks for each translation entry that needs to be imported.
     *
     * @param installContext the installation context providing access to the JCR session
     * @throws TaskExecutionException if an error occurs during task execution
     */
    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        ArrayDelegateTask task = new ArrayDelegateTask("Add translation key tasks");
        try {
            addTranslationNodeTasks(task, installContext);
        } catch (ResourceOrigin.ResourceNotFoundException e) {
            installContext.warn(e.getLocalizedMessage());
            LOGGER.error("Error reading translation message bundle.", e);
        }
        task.execute(installContext);
    }

    /**
     * Reads the resource bundle and creates tasks for each translation key that doesn't already exist.
     *
     * @param task the delegate task to which individual translation import tasks are added
     * @param installContext the installation context for warnings and errors
     */
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

    /**
     * Loads a resource bundle from the given resource and applies the consumer to it.
     *
     * @param resource the resource to load as a property resource bundle
     * @param bundleConsumer the consumer that processes the loaded resource bundle
     */
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
     * Retrieves the properties file resource for the specified locale.
     *
     * @param locale the locale string (e.g., "de", "en_US")
     * @return the resource representing the properties file
     */
    protected Resource getResource(String locale) {
        final ResourceOrigin resourceOrigins = Components.getComponent(ResourceOrigin.class);
        return resourceOrigins.getByPath("/" + _baseName.replace(".", "/") + "_" + locale + ".properties");
    }

    /**
     * Returns the node name helper used for generating valid JCR node names.
     *
     * @return the node name helper instance
     */
    public NodeNameHelper getNodeNameHelper() {
        return _nodeNameHelper;
    }

    /**
     * Returns the base path where translation nodes are created.
     *
     * @return the base path in the translation workspace
     */
    public String getBasePath() {
        return _basePath;
    }
}
