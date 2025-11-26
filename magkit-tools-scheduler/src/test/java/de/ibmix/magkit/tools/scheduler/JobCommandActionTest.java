package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * magkit-tools-scheduler
 * %%
 * Copyright (C) 2025 IBM iX
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

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.commands.CommandsManager;
import info.magnolia.context.Context;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.module.scheduler.JobDefinition;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.CommandActionDefinition;
import info.magnolia.ui.contentapp.async.AsyncActionExecutor;
import info.magnolia.ui.observation.DatasourceObservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JobCommandAction} focusing on command execution paths and parameter merging.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-19
 */
class JobCommandActionTest {

    private CommandsManager _commandsManager;
    private Node2BeanProcessor _node2BeanProcessor;
    private CommandActionDefinition _definition;
    private ValueContext<Node> _valueContext;
    private Context _context;
    private AsyncActionExecutor _asyncExecutor;
    private DatasourceObservation.Manual _datasourceObservation;

    /**
     * Initializes common test fixtures and default action definition.
     */
    @BeforeEach
    void setUp() {
        _datasourceObservation = ComponentsMockUtils.mockComponentInstance(DatasourceObservation.Manual.class);
        _commandsManager = mock(CommandsManager.class);
        _node2BeanProcessor = mock(Node2BeanProcessor.class);
        _definition = new CommandActionDefinition();
        _definition.setCatalog("defCatalog");
        _definition.setCommand("defCommand");
        _valueContext = mock(ValueContext.class);
        _context = mock(Context.class);
        _asyncExecutor = mock(AsyncActionExecutor.class);
    }

    /**
     * Verifies job definition catalog/command override action definition and parameters merged.
     */
    @Test
    void executeCommandUsesJobDefinitionCatalogCommandAndParams() throws Exception {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setCatalog("jobCatalog");
        jobDefinition.setCommand("jobCommand");
        Map<String, Object> jobParams = new HashMap<>();
        jobParams.put("p1", "v1");
        jobDefinition.setParams(jobParams);

        when(_node2BeanProcessor.toBean(any(Node.class), eq(JobDefinition.class))).thenReturn(jobDefinition);
        when(_commandsManager.executeCommand(eq("jobCatalog"), eq("jobCommand"), any(Map.class))).thenReturn(true);

        Node node = mockNode("jobs", "/path/job");
        TestableJobCommandAction action = new TestableJobCommandAction(_definition, _commandsManager, _valueContext, _context, _asyncExecutor, _node2BeanProcessor);
        action.setActionParams(Map.of("a", "1"));

        assertTrue(action.executeForTest(node));
        verify(_commandsManager).executeCommand(eq("jobCatalog"), eq("jobCommand"), any(Map.class));
        verify(_datasourceObservation).trigger();
    }

    /**
     * Verifies fallback to action definition catalog/command when job definition omits them.
     */
    @Test
    void executeCommandFallsBackToDefinitionWhenJobDefinitionMissingFields() throws Exception {
        JobDefinition jobDefinition = new JobDefinition();
        Map<String, Object> jobParams = new HashMap<>();
        jobParams.put("x", "y");
        jobDefinition.setParams(jobParams);

        when(_node2BeanProcessor.toBean(any(Node.class), eq(JobDefinition.class))).thenReturn(jobDefinition);
        when(_commandsManager.executeCommand(eq("defCatalog"), eq("defCommand"), any(Map.class))).thenReturn(true);

        Node node = mockNode("jobs", "/path/job");
        TestableJobCommandAction action = new TestableJobCommandAction(_definition, _commandsManager, _valueContext, _context, _asyncExecutor, _node2BeanProcessor);
        action.setActionParams(Map.of("base", "value"));

        assertTrue(action.executeForTest(node));
        verify(_commandsManager).executeCommand(eq("defCatalog"), eq("defCommand"), any(Map.class));
    }

    /**
     * Verifies job params overwrite action params on key collision and merge retains all keys.
     */
    @Test
    void executeCommandMergesParamsAndJobParamsJobOverridesAction() throws Exception {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setCatalog("jobCatalog");
        jobDefinition.setCommand("jobCommand");
        Map<String, Object> jobParams = new HashMap<>();
        jobParams.put("override", "jobValue");
        jobParams.put("onlyJob", "x");
        jobDefinition.setParams(jobParams);

        when(_node2BeanProcessor.toBean(any(Node.class), eq(JobDefinition.class))).thenReturn(jobDefinition);
        when(_commandsManager.executeCommand(eq("jobCatalog"), eq("jobCommand"), any(Map.class))).thenAnswer(invocation -> {
            Map<String, Object> params = invocation.getArgument(2);
            return "jobValue".equals(params.get("override")) && "x".equals(params.get("onlyJob")) && "1".equals(params.get("base"));
        });

        Node node = mockNode("jobs", "/path/job");
        TestableJobCommandAction action = new TestableJobCommandAction(_definition, _commandsManager, _valueContext, _context, _asyncExecutor, _node2BeanProcessor);
        action.setActionParams(Map.of("base", "1", "override", "actionValue"));

        assertTrue(action.executeForTest(node));
    }

    /**
     * Verifies false return is propagated when CommandsManager returns false.
     */
    @Test
    void executeCommandReturnsFalseWhenCommandManagerReturnsFalse() throws Exception {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setCatalog("jobCatalog");
        jobDefinition.setCommand("jobCommand");
        when(_node2BeanProcessor.toBean(any(Node.class), eq(JobDefinition.class))).thenReturn(jobDefinition);
        when(_commandsManager.executeCommand(eq("jobCatalog"), eq("jobCommand"), any(Map.class))).thenReturn(false);

        Node node = mockNode("jobs", "/path/job");
        TestableJobCommandAction action = new TestableJobCommandAction(_definition, _commandsManager, _valueContext, _context, _asyncExecutor, _node2BeanProcessor);
        action.setActionParams(Map.of());

        assertFalse(action.executeForTest(node));
    }

    /**
     * Verifies exceptions from Node2BeanProcessor propagate out of executeCommand.
     */
    @Test
    void executeCommandPropagatesExceptionFromNode2BeanProcessor() throws Exception {
        when(_node2BeanProcessor.toBean(any(Node.class), eq(JobDefinition.class))).thenThrow(new RuntimeException("boom"));
        Node node = mockNode("jobs", "/path/job");
        TestableJobCommandAction action = new TestableJobCommandAction(_definition, _commandsManager, _valueContext, _context, _asyncExecutor, _node2BeanProcessor);
        assertThrows(Exception.class, () -> action.executeForTest(node));
    }

    /**
     * Verifies only action params are used when jobParams null.
     */
    @Test
    void executeCommandWithEmptyJobParamsUsesOnlyActionParams() throws Exception {
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setCatalog("jobCatalog");
        jobDefinition.setCommand("jobCommand");
        when(_node2BeanProcessor.toBean(any(Node.class), eq(JobDefinition.class))).thenReturn(jobDefinition);
        when(_commandsManager.executeCommand(eq("jobCatalog"), eq("jobCommand"), any(Map.class))).thenAnswer(invocation -> {
            Map<String, Object> params = invocation.getArgument(2);
            return params.size() == 1 && "1".equals(params.get("base"));
        });

        Node node = mockNode("jobs", "/path/job");
        TestableJobCommandAction action = new TestableJobCommandAction(_definition, _commandsManager, _valueContext, _context, _asyncExecutor, _node2BeanProcessor);
        action.setActionParams(Map.of("base", "1"));

        assertTrue(action.executeForTest(node));
    }

    /**
     * Testable subclass exposing protected methods and allowing injection of action params.
     */
    static class TestableJobCommandAction extends JobCommandAction {
        private Map<String, Object> _actionParams = new HashMap<>();
        TestableJobCommandAction(CommandActionDefinition definition, CommandsManager commandsManager, ValueContext<Node> valueContext, Context context, AsyncActionExecutor asyncActionExecutor, Node2BeanProcessor nodeToBeanProcessor) {
            super(definition, commandsManager, valueContext, context, asyncActionExecutor, nodeToBeanProcessor);
        }
        /**
         * Sets base action params used by buildParams.
         */
        void setActionParams(Map<String, Object> actionParams) {
            _actionParams = new HashMap<>(actionParams);
        }
        /**
         * Returns a copy of configured action params.
         */
        @Override
        protected Map<String, Object> buildParams(Node node) {
            return new HashMap<>(_actionParams);
        }
        /**
         * Exposes protected executeCommand for direct testing.
         */
        boolean executeForTest(Node node) throws Exception {
            return executeCommand(node);
        }
    }
}
