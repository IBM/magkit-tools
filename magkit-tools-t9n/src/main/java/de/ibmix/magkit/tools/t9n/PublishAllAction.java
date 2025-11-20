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

import com.google.inject.Inject;
import de.ibmix.magkit.tools.t9n.setup.TranslationModule;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.action.JcrCommandAction;
import info.magnolia.ui.contentapp.action.JcrCommandActionDefinition;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collection;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static org.apache.commons.lang3.StringUtils.startsWith;

/**
 * Action for publishing all translation nodes from a configured base path.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * Activates all translation nodes within the configured base path to make them available
 * on public instances. This action simplifies the workflow of publishing multiple translations
 * at once without having to select and publish them individually.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Publishes all translation nodes from the configured base path</li>
 * <li>Respects the module's base path configuration</li>
 * <li>Uses Magnolia's standard activation mechanism</li>
 * <li>Processes nodes asynchronously for better performance</li>
 * </ul>
 * <p>
 * <p><strong>Usage:</strong></p>
 * This action is typically configured as a toolbar action in the translation app
 * and can be triggered to publish all translations at once.
 * <p>
 * <p><strong>Preconditions:</strong></p>
 * Requires proper publishing permissions and configured public instances.
 *
 * @author Janine.Naumann
 * @since 2023-01-01
 */
@Slf4j
public class PublishAllAction extends JcrCommandAction<Node, JcrCommandActionDefinition> {

    /**
     * Creates a new publish all action with all required dependencies.
     *
     * @param definition the action definition configuration
     * @param commandsManager the manager for executing JCR commands
     * @param valueContext the context providing access to the current node
     * @param context the Magnolia context
     * @param asyncActionExecutor the executor for asynchronous action execution
     * @param datasource the JCR datasource
     */
    @Inject
    public PublishAllAction(JcrCommandActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext, Context context, AsyncActionExecutor asyncActionExecutor, JcrDatasource datasource) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor, datasource);
    }

    /**
     * Resolves all translation nodes from the configured base path that should be published.
     *
     * @return a collection of all translation nodes to be activated
     */
    @Override
    protected Collection<Node> resolveTargetItems() {
        Collection<Node> items = new ArrayList<>();

        String path = getBasePath();
        try {
            Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
            Node rootNode = session.getNode(path);
            Iterable<Node> nodesToActivate = NodeUtil.getNodes(rootNode, TranslationNodeTypes.Translation.NAME);
            for (Node node : nodesToActivate) {
                items.add(node);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error getting all child items.", e);
        }

        return items;
    }

    String getBasePath() {
        final TranslationModule module = Components.getComponent(TranslationModule.class);
        String path = "/";
        if (startsWith(module.getBasePath(), "/")) {
            path = module.getBasePath();
        }
        return path;
    }
}
