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
 * Task for adding missing translations from legacy properties to app.
 *
 * @author frank.sommer
 * @since 27.11.2017
 */
public class AddTranslationsTask extends AddTranslationEntryTask {
    private final Locale[] _locales;

    public AddTranslationsTask(String baseName, Locale... locales) {
        this("Add translations for " + baseName,
            "Add translations for " + baseName + " in locales " + join(locales, ", "),
            baseName,
            EMPTY,
            locales
        );
    }

    public AddTranslationsTask(String name, String description, String baseName, String basePath, Locale... locales) {
        super(name, description, baseName, locales[0], basePath);
        _locales = Arrays.copyOfRange(locales, 1, locales.length);
    }

    @Override
    protected void addTranslationNodeTasks(ArrayDelegateTask task, InstallContext installContext) {
        super.addTranslationNodeTasks(task, installContext);
        for (Locale locale : _locales) {
            addTasksForResource(getResource(locale.toString()), new ResourceBundleConsumer(locale, installContext, task));
        }
    }

    private class ResourceBundleConsumer implements Consumer<ResourceBundle> {
        private final Locale _locale;
        private final InstallContext _installContext;
        private final ArrayDelegateTask _task;

        ResourceBundleConsumer(Locale locale, InstallContext installContext, ArrayDelegateTask task) {
            _locale = locale;
            _installContext = installContext;
            _task = task;
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

        private void addTaskForKey(ResourceBundle bundle, String key, Session session) {
            String keyNodeName = AddTranslationsTask.this.getNodeNameHelper().getValidatedName(key);
            String nodePath = AddTranslationsTask.this.getBasePath() + keyNodeName;

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

        private Task createAddTranslationOperation(String nodePath, String key, String propertyName, String translation) {
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
