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
import info.magnolia.jcr.nodebuilder.AbstractNodeOperation;
import info.magnolia.jcr.nodebuilder.ErrorHandler;
import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PN_KEY;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.nodebuilder.Ops.addProperty;
import static info.magnolia.jcr.nodebuilder.task.ErrorHandling.logging;
import static info.magnolia.jcr.util.NodeTypes.LastModified.LAST_MODIFIED;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

/**
 * Enhanced translation import task that handles multiple locales and updates existing translations.
 * <p><strong>Purpose:</strong></p>
 * Extends {@link AddTranslationEntryTask} to support importing translations for multiple locales
 * at once and intelligently updating existing translation nodes with missing locale values.
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Imports translations for multiple locales in a single task</li>
 * <li>Creates new translation nodes where needed</li>
 * <li>Updates existing nodes by adding missing locale properties</li>
 * <li>Skips properties that already have values to preserve manual edits</li>
 * <li>Supports custom base paths for organizing translations</li>
 * </ul>
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * Task task = new AddTranslationsTask(
 *     "com.example.messages",
 *     Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH
 * );
 * </pre>
 * <p><strong>Migration Use Case:</strong></p>
 * This task is particularly useful for migrating from legacy property file-based translations
 * to the JCR-based translation workspace.
 *
 * @author frank.sommer
 * @since 2017-11-27
 */
public class AddTranslationsTask extends AddTranslationEntryTask {
    private final Locale[] _locales;

    /**
     * Creates a new task to import translations for multiple locales.
     *
     * @param baseName the fully qualified base name of the resource bundle (e.g., "com.example.messages")
     * @param locales the locales for which to import translations (at least one required)
     */
    public AddTranslationsTask(String baseName, Locale... locales) {
        this("Add translations for " + baseName,
            "Add translations for " + baseName + " in locales " + join(locales, ", "),
            baseName,
            EMPTY,
            locales
        );
    }

    /**
     * Creates a new task to import translations for multiple locales with custom name and base path.
     *
     * @param name the display name of the task
     * @param description the detailed description of what the task does
     * @param baseName the fully qualified base name of the resource bundle
     * @param basePath the base path in the translation workspace where nodes will be created
     * @param locales the locales for which to import translations (at least one required)
     */
    public AddTranslationsTask(String name, String description, String baseName, String basePath, Locale... locales) {
        super(name, description, baseName, locales[0], basePath);
        _locales = Arrays.copyOfRange(locales, 1, locales.length);
    }

    /**
     * Processes all locales and creates tasks for creating or updating translation nodes.
     * For each locale, checks existing nodes and only adds missing properties.
     *
     * @param task the delegate task to which individual translation import tasks are added
     * @param installContext the installation context for warnings and errors
     */
    @Override
    protected void addTranslationNodeTasks(ArrayDelegateTask task, InstallContext installContext) {
        super.addTranslationNodeTasks(task, installContext);
        for (Locale locale : _locales) {
            addTasksForResource(getResource(locale.toString()), new ResourceBundleConsumer(locale, installContext, task, getBasePath(), getNodeNameHelper()));
        }
    }

    static class ResourceBundleConsumer implements Consumer<ResourceBundle> {
        private final Locale _locale;
        private final InstallContext _installContext;
        private final ArrayDelegateTask _task;
        private final String _basePath;
        private final NodeNameHelper _nodeNameHelper;

        ResourceBundleConsumer(Locale locale, InstallContext installContext, ArrayDelegateTask task, String basePath, NodeNameHelper nodeNameHelper) {
            _locale = locale;
            _installContext = installContext;
            _task = task;
            _basePath = basePath;
            _nodeNameHelper = nodeNameHelper;
        }

        @Override
        public void accept(ResourceBundle bundle) {
            try {
                Session session = _installContext.getJCRSession(WS_TRANSLATION);
                for (String key : bundle.keySet()) {
                    addTaskForKey(bundle, key, session);

                }
            } catch (RepositoryException e) {
                _installContext.error("Can not get session for: " + WS_TRANSLATION, e);
            }
        }

        void addTaskForKey(ResourceBundle bundle, String key, Session session) {
            String keyNodeName = _nodeNameHelper.getValidatedName(key);
            String nodePath = _basePath + keyNodeName;

            try {
                final String propertyName = Translation.PREFIX_NAME + _locale;
                final String translation = trimToEmpty(bundle.getString(key));

                if (session.itemExists(nodePath)) {
                    Node translationNode = session.getNode(nodePath);
                    String currentTranslation = getString(translationNode, propertyName, EMPTY);
                    if (isNotEmpty(currentTranslation)) {
                        _installContext.info("Translation already set, skip " + keyNodeName + " ...");
                    } else {
                        _task.addTask(createAddTranslationOperation(nodePath, key, propertyName, translation));
                    }
                } else {
                    _task.addTask(createAddTranslationOperation(nodePath, key, propertyName, translation));
                }
            } catch (RepositoryException e) {
                _installContext.error("Can not write translation for key: " + key, e);
            }
        }

        Task createAddTranslationOperation(String nodePath, String key, String propertyName, String translation) {
            String name = removeStart(nodePath, ROOT_PATH);
            return new NodeBuilderTask("Add translation for probably existing node", "", logging, WS_TRANSLATION,
                new AbstractNodeOperation() {

                    @Override
                    protected Node doExec(Node context, ErrorHandler errorHandler) throws RepositoryException {
                        Node t9nNode;
                        if (context.hasNode(name)) {
                            t9nNode = context.getNode(name);
                        } else {
                            t9nNode = context.addNode(name, Translation.NAME);
                            t9nNode.setProperty(PN_KEY, key);
                            t9nNode.setProperty(LAST_MODIFIED, Calendar.getInstance());
                        }
                        return t9nNode;
                    }
                }.then(
                    addProperty(propertyName, (Object) translation)
                )
            );
        }
    }
}
