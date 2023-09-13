package de.ibmix.magkit.scheduler;

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
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.contentapp.action.CommandAction;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.Map;
import java.util.Optional;

/**
 * Action definition to execute the command from the selected job.
 *
 * @author frank.sommer
 * @since 25.08.2023
 */
@Slf4j
public class JobCommandAction extends CommandAction<Node, CommandActionDefinition> {

    private final CommandsManager _commandsManager;
    private final Node2BeanProcessor _nodeToBeanProcessor;

    @Inject
    public JobCommandAction(CommandActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext, Context context, AsyncActionExecutor asyncActionExecutor, Node2BeanProcessor nodeToBeanProcessor) {
        super(definition, commandsManager, valueContext, context, asyncActionExecutor);
        _commandsManager = commandsManager;
        _nodeToBeanProcessor = nodeToBeanProcessor;
    }

    @Override
    protected boolean executeCommand(Node node) throws Exception {
        final JobDefinition jobDefinition = (JobDefinition) _nodeToBeanProcessor.toBean(node, JobDefinition.class);

        final String catalog = Optional.ofNullable(jobDefinition.getCatalog()).orElse(getDefinition().getCatalog());
        final String command = Optional.ofNullable(jobDefinition.getCommand()).orElse(getDefinition().getCommand());
        final Map<String, Object> params = buildParams(node);
        Optional.ofNullable(jobDefinition.getParams()).ifPresent(params::putAll);

        return _commandsManager.executeCommand(catalog, command, params);
    }
}
