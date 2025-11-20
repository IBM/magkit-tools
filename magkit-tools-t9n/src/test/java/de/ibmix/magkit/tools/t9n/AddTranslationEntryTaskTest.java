package de.ibmix.magkit.tools.t9n;

/*-
 * #%L
 * magkit-tools-t9n
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


import info.magnolia.jcr.nodebuilder.task.NodeBuilderTask;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.Task;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryResult;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockInstallContext;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * Tests for {@link AddTranslationEntryTask} covering constructor normalization, branching logic and error handling.
 *
 * @author wolf.bubenik (IBM iX)
 * @since 2025-11-20
 */
public class AddTranslationEntryTaskTest {

    /**
     * Verifies basePath normalization in the constructor for root path.
     */
    @Test
    public void shouldNormalizeRootBasePath() {
        NodeNameHelper helper = mockComponentInstance(NodeNameHelper.class);
        AddTranslationEntryTask task = new AddTranslationEntryTask("name", "desc", "com.example.messages", Locale.ENGLISH, "");
        assertEquals("/", task.getBasePath());
        assertSame(helper, task.getNodeNameHelper());
    }

    /**
     * Verifies basePath normalization for a relative path without leading slash.
     */
    @Test
    public void shouldNormalizeRelativeBasePath() {
        mockComponentInstance(NodeNameHelper.class);
        AddTranslationEntryTask task = new AddTranslationEntryTask("name", "desc", "com.example.messages", Locale.ENGLISH, "foo");
        assertEquals("foo/", task.getBasePath());
    }

    /**
     * Verifies basePath normalization for a path with leading slash.
     */
    @Test
    public void shouldKeepLeadingSlashInBasePath() {
        mockComponentInstance(NodeNameHelper.class);
        AddTranslationEntryTask task = new AddTranslationEntryTask("name", "desc", "com.example.messages", Locale.ENGLISH, "/foo");
        assertEquals("/foo/", task.getBasePath());
    }

    /**
     * Covers createTranslationEntryOperation branch for root base path.
     */
    @Test
    public void shouldCreateTranslationEntryOperationForRootPath() {
        NodeNameHelper nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(nodeNameHelper.getValidatedName("key.one")) .thenReturn("key.one");
        AddTranslationEntryTask task = new AddTranslationEntryTask("name", "desc", "com.example.messages", Locale.ENGLISH, "");
        Task subTask = task.createTranslationEntryOperation("key.one", "key.one", "Value One");
        assertNotNull(subTask);
        assertInstanceOf(NodeBuilderTask.class, subTask);

    }

    /**
     * Covers createTranslationEntryOperation branch for custom base path.
     */
    @Test
    public void shouldCreateTranslationEntryOperationForCustomPath() {
        NodeNameHelper nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(nodeNameHelper.getValidatedName("key.two")) .thenReturn("key.two");
        AddTranslationEntryTask task = new AddTranslationEntryTask("name", "desc", "com.example.messages", Locale.ENGLISH, "foo");
        Task subTask = task.createTranslationEntryOperation("key.two", "key.two", "Value Two");
        assertNotNull(subTask);
        assertInstanceOf(NodeBuilderTask.class, subTask);
    }

    /**
     * Tests addTasksForResource successful path reading two keys.
     */
    @Test
    public void shouldProcessResourceBundle() throws IOException {
        mockComponentInstance(NodeNameHelper.class);
        AddTranslationEntryTask task = new AddTranslationEntryTask("n", "d", "com.example.messages", Locale.ENGLISH);
        Resource resource = mock(Resource.class);
        String properties = "key.one=Value One\nkey.two=Value Two";
        when(resource.openStream()).thenReturn(new ByteArrayInputStream(properties.getBytes(StandardCharsets.UTF_8)));
        List<String> processedKeys = new ArrayList<>();
        task.addTasksForResource(resource, bundle -> processedKeys.addAll(bundle.keySet()));
        assertEquals(2, processedKeys.size());
        assertTrue(processedKeys.contains("key.one"));
        assertTrue(processedKeys.contains("key.two"));
    }

    /**
     * Tests addTasksForResource IOException branch.
     */
    @Test
    public void shouldHandleIOExceptionOnResource() throws IOException {
        mockComponentInstance(NodeNameHelper.class);
        AddTranslationEntryTask task = new AddTranslationEntryTask("n", "d", "com.example.messages", Locale.ENGLISH);
        Resource resource = mock(Resource.class);
        when(resource.openStream()).thenThrow(new IOException("boom"));
        List<String> processedKeys = new ArrayList<>();
        assertDoesNotThrow(() -> task.addTasksForResource(resource, bundle -> processedKeys.addAll(bundle.keySet())));
        assertTrue(processedKeys.isEmpty());
    }

    /**
     * Tests keyExists positive, negative and exception branches by static mocking QueryUtil.search.
     */
    @Test
    public void shouldEvaluateKeyExistsVariants() throws RepositoryException {
        Node existingNode = mock(Node.class);
        mockComponentInstance(NodeNameHelper.class);
        AddTranslationEntryTask task = new AddTranslationEntryTask("n", "d", "com.example.messages", Locale.ENGLISH);
        mockQueryResult(TranslationNodeTypes.WS_TRANSLATION, Query.JCR_SQL2, MagnoliaTranslationServiceImpl.BASE_QUERY + "'a.key'", existingNode);
        assertTrue(task.keyExists("a.key"));

        mockQueryResult(TranslationNodeTypes.WS_TRANSLATION, Query.JCR_SQL2, MagnoliaTranslationServiceImpl.BASE_QUERY + "'b.key'");
        assertFalse(task.keyExists("b.key"));

        QueryResult queryResult = mockQueryResult(TranslationNodeTypes.WS_TRANSLATION, Query.JCR_SQL2, MagnoliaTranslationServiceImpl.BASE_QUERY + "'c.key'");
        doThrow(new RepositoryException("fail")).when(queryResult).getNodes();
        assertFalse(task.keyExists("c.key"));
    }

    /**
     * Tests addTranslationNodeTasks adds tasks only for keys that do not exist.
     */
    @Test
    public void shouldAddTasksOnlyForMissingKeys() {
        NodeNameHelper nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(nodeNameHelper.getValidatedName("key.one")).thenReturn("key.one");
        when(nodeNameHelper.getValidatedName("key.two")).thenReturn("key.two");
        TestableAddTranslationEntryTask task = new TestableAddTranslationEntryTask("n", "d", "base.name", Locale.ENGLISH, "/", Set.of("key.one"));
        ArrayDelegateTask delegate = new ArrayDelegateTask("Add translation key tasks");
        task.addTranslationNodeTasks(delegate, mock(InstallContext.class));
        assertEquals(1, task._createdTasks.size());
    }

    /**
     * Tests execute path when resource is found and a subtask executes.
     */
    @Test
    public void shouldExecuteDelegateTasksOnSuccess() throws Exception {
        NodeNameHelper nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(nodeNameHelper.getValidatedName("key.one")).thenReturn("key.one");
        TestableAddTranslationEntryTask task = new TestableAddTranslationEntryTask("n", "d", "base.name", Locale.ENGLISH, "/", Set.of());
        task._resourceContent = "key.one=Value One";
        AtomicBoolean executed = new AtomicBoolean(false);
        task._customTaskFactory = () -> new AbstractTask("dummy", "") {
            @Override
            public void execute(InstallContext installContext) {
                executed.set(true);
            }
        };
        InstallContext ctx = mock(InstallContext.class);
        task.execute(ctx);
        assertTrue(executed.get());
        verify(ctx, times(0)).warn(anyString());
    }

    /**
     * Tests execute path handling ResourceNotFoundException with warning and no subtasks executed.
     */
    @Test
    public void shouldHandleResourceNotFoundInExecute() throws Exception {
        mockComponentInstance(NodeNameHelper.class);
        TestableAddTranslationEntryTask task = new TestableAddTranslationEntryTask("n", "d", "base.name", Locale.ENGLISH, "/", Set.of());
        task._throwNotFound = true;
        InstallContext ctx = mockInstallContext();
        task.execute(ctx);
        verify(ctx, times(1)).warn(anyString());
        assertTrue(task._createdTasks.isEmpty());
    }

    /**
     * Tests getResource building the correct path using baseName and locale.
     */
    @Test
    public void shouldResolveResourcePath() {
        mockComponentInstance(NodeNameHelper.class);
        @SuppressWarnings("unchecked")ResourceOrigin<Resource> origin = mockComponentInstance(ResourceOrigin.class);
        Resource resource = mock(Resource.class);
        when(origin.getByPath(anyString())).thenReturn(resource);
        AddTranslationEntryTask task = new AddTranslationEntryTask("n", "d", "com.example.messages", Locale.GERMAN);
        Resource resolved = task.getResource("de");
        assertNotNull(resolved);
        verify(origin).getByPath("/com/example/messages_de.properties");
    }

    /**
     * Testable subclass exposing hooks for test control.
     */
    static class TestableAddTranslationEntryTask extends AddTranslationEntryTask {

        final Set<String> _existingKeys;
        final List<Task> _createdTasks = new ArrayList<>();
        String _resourceContent = "key.one=Value One\nkey.two=Value Two";
        boolean _throwNotFound;
        java.util.function.Supplier<Task> _customTaskFactory = () -> null;

        TestableAddTranslationEntryTask(String taskName, String taskDescription, String baseName, Locale locale, String basePath, Set<String> existingKeys) {
            super(taskName, taskDescription, baseName, locale, basePath);
            _existingKeys = new HashSet<>(existingKeys);
        }

        @Override
        protected Resource getResource(String locale) {
            if (_throwNotFound) {
                ResourceOrigin<?> origin = mock(ResourceOrigin.class);
                throw new ResourceOrigin.ResourceNotFoundException(origin, "/nf");
            }
            Resource resource = mock(Resource.class);
            InputStream stream = new ByteArrayInputStream(_resourceContent.getBytes(StandardCharsets.UTF_8));
            try {
                when(resource.openStream()).thenReturn(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return resource;
        }

        @Override
        boolean keyExists(String key) {
            return _existingKeys.contains(key);
        }

        @Override
        Task createTranslationEntryOperation(String keyNodeName, String key, String value) {
            Task original = super.createTranslationEntryOperation(keyNodeName, key, value);
            _createdTasks.add(original);
            Task custom = _customTaskFactory.get();
            if (custom != null) {
                _createdTasks.add(custom);
                return custom;
            }
            return original;
        }
    }
}
