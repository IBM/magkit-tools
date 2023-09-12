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

import de.ibmix.magkit.tools.t9n.setup.TranslationModule;
import com.google.inject.Inject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.Collection;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static org.apache.commons.lang3.StringUtils.startsWith;

/**
 * Get parent translation folder node and activate all sub nodes recursive.
 *
 * @author Janine.Naumann
 */
public class PublishAllAction extends JcrCommandAction<Node, JcrCommandActionDefinition> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishAllAction.class);

    @Inject
    public PublishAllAction(JcrCommandActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext, Context context, AsyncActionExecutor asyncActionExecutor, JcrDatasource datasource) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor, datasource);
    }

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

    private String getBasePath() {
        final TranslationModule module = Components.getComponent(TranslationModule.class);
        String path = "/";
        if (startsWith(module.getBasePath(), "/")) {
            path = module.getBasePath();
        }
        return path;
    }
}