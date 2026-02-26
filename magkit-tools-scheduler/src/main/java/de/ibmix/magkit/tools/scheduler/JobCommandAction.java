package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * magkit-scheduler
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

import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.module.scheduler.JobDefinition;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.contentapp.action.CommandAction;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.observation.DatasourceObservation;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Node;
import java.util.Map;
import java.util.Optional;

/**
 * Action to execute the command associated with a selected Magnolia scheduler job.
 * This action extends the standard CommandAction to specifically handle job definitions stored in JCR nodes.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Extracts job configuration from JCR nodes using Node2BeanProcessor</li>
 * <li>Executes commands with catalog and command name from job definition</li>
 * <li>Merges job-specific parameters with action parameters</li>
 * <li>Falls back to definition defaults if job configuration is incomplete</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * This action is typically configured in a content app definition for the scheduler module,
 * allowing users to manually trigger job commands from the Magnolia AdminCentral UI.
 *
 * @author frank.sommer
 * @since 2023-08-25
 */
@Slf4j
public class JobCommandAction extends CommandAction<Node, CommandActionDefinition> {

    private final CommandsManager _commandsManager;
    private final Node2BeanProcessor _nodeToBeanProcessor;
    private final DatasourceObservation.Manual _datasourceObservation;

    /**
     * Creates a new JobCommandAction instance with required dependencies.
     *
     * @param definition the command action definition containing configuration
     * @param commandsManager the manager for executing Magnolia commands
     * @param valueContext the context providing access to selected JCR nodes
     * @param context the Magnolia context for command execution
     * @param asyncActionExecutor the executor for asynchronous action execution
     * @param nodeToBeanProcessor the processor for converting JCR nodes to Java beans
     */
    @Inject
    public JobCommandAction(CommandActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext, Context context, AsyncActionExecutor asyncActionExecutor, Node2BeanProcessor nodeToBeanProcessor) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor);
        _commandsManager = commandsManager;
        _nodeToBeanProcessor = nodeToBeanProcessor;
        _datasourceObservation = Components.getComponent(DatasourceObservation.Manual.class);
    }

    /**
     * Executes the command configured in the given job definition node.
     * Converts the JCR node to a JobDefinition bean, extracts command details,
     * merges parameters, and executes the command via CommandsManager.
     *
     * @param node the JCR node representing the job definition
     * @return true if the command was executed successfully, false otherwise
     * @throws Exception if an error occurs during job definition conversion or command execution
     */
    @Override
    protected boolean executeCommand(Node node) throws Exception {
        final JobDefinition jobDefinition = (JobDefinition) _nodeToBeanProcessor.toBean(node, JobDefinition.class);

        final String catalog = Optional.ofNullable(jobDefinition.getCatalog()).orElse(getDefinition().getCatalog());
        final String command = Optional.ofNullable(jobDefinition.getCommand()).orElse(getDefinition().getCommand());
        final Map<String, Object> params = buildParams(node);
        Optional.ofNullable(jobDefinition.getParams()).ifPresent(params::putAll);

        boolean result = _commandsManager.executeCommand(catalog, command, params);
        _datasourceObservation.trigger();
        return result;
    }
}
