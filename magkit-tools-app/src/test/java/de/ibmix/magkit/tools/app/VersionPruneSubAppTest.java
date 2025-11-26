package de.ibmix.magkit.tools.app;

/*-
 * #%L
 * magkit-tools-app
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

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import de.ibmix.magkit.test.jcr.SessionMockUtils;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import org.apache.jackrabbit.commons.iterator.VersionIteratorAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import java.util.Arrays;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for VersionPruneSubApp.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-18
 */
class VersionPruneSubAppTest {

    private static final String WORKSPACE = "website";
    private static final String NODE_PATH = "/test/node";
    private static final String VERSION_NAME = "1.0";

    private VersionPruneResultView _view;
    private SimpleTranslator _simpleTranslator;
    private Context _context;
    private VersionPruneSubApp _versionPruneSubApp;
    private Item _item;

    @BeforeEach
    void setUp() throws Exception {
        mockWebContext();

        SubAppContext subAppContext = mock(SubAppContext.class);
        FormViewReduced formView = mock(FormViewReduced.class);
        _view = mock(VersionPruneResultView.class);
        FormBuilder builder = mock(FormBuilder.class);
        _simpleTranslator = mock(SimpleTranslator.class);
        Provider<Context> contextProvider = mock(Provider.class);
        _context = mock(Context.class);
        _item = mock(Item.class);

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        FormDefinition formDefinition = mock(FormDefinition.class);

        when(subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(formDefinition);
        when(contextProvider.get()).thenReturn(_context);
        when(formView.getItemDataSource()).thenReturn(_item);

        _versionPruneSubApp = new VersionPruneSubApp(subAppContext, formView, _view, builder, _simpleTranslator, contextProvider);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void constructorInitializesFields() {
        assertInstanceOf(ToolsBaseSubApp.class, _versionPruneSubApp);
    }

    @Test
    void doActionWithValidNodeAndVersions() throws Exception {
        mockItemProperties(NODE_PATH, "0");

        Node node = MagnoliaNodeMockUtils.mockPageNode(NODE_PATH);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);

        mockVersionHistory(node);

        when(_simpleTranslator.translate("versionPrune.nothingPruned")).thenReturn("Nothing pruned");

        _versionPruneSubApp.doAction();

        verify(_view).buildResultView("\n\n-------------------------------------------------------------\nNothing pruned\n\n-------------------------------------------------------------\n");
    }

    @Test
    void doActionWithNonExistentNode() throws Exception {
        mockItemProperties("/non/existent", "2");

        Session session = SessionMockUtils.mockSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);
        when(session.getNode("/non/existent")).thenThrow(new RepositoryException("Node not found"));
        when(_simpleTranslator.translate(eq("versionPrune.path.wrong"), anyString(), anyString())).thenReturn("Wrong path");

        _versionPruneSubApp.doAction();

        verify(_view).buildResultView(anyString());
    }

    @Test
    void doActionWithNoPrunedVersions() throws Exception {
        mockItemProperties(NODE_PATH, "10");

        Node node = MagnoliaNodeMockUtils.mockPageNode(NODE_PATH);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);

        mockVersionHistory(node);

        when(_simpleTranslator.translate("versionPrune.nothingPruned.prefix")).thenReturn("No versions were pruned. Keeping");
        when(_simpleTranslator.translate("versionPrune.nothingPruned.postfix")).thenReturn("versions");

        _versionPruneSubApp.doAction();

        verify(_view).buildResultView("\n\n-------------------------------------------------------------\nNo versions were pruned. Keeping 10 versions\n\n-------------------------------------------------------------\n");
    }

    @Test
    void handleNodeWithVersionHistory() throws Exception {
        Node node = MagnoliaNodeMockUtils.mockPageNode(NODE_PATH);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);

        VersionHistory versionHistory = mockVersionHistoryWithNames(node, VERSION_NAME, "1.1", "1.2", "1.3");

        _versionPruneSubApp.handleNode(node, 2);

        verify(versionHistory).removeVersion("1.1");
    }

    @Test
    void handleNodeWithNoVersionHistory() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);

        VersionManager versionManager = ComponentsMockUtils.mockComponentInstance(VersionManager.class);
        when(versionManager.getVersionHistory(node)).thenThrow(new RepositoryException("No version history"));

        _versionPruneSubApp.handleNode(node, 2);

        verify(versionManager).getVersionHistory(node);
    }

    @Test
    void handleNodeWithNullVersionHistory() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);

        VersionManager versionManager = ComponentsMockUtils.mockComponentInstance(VersionManager.class);

        _versionPruneSubApp.handleNode(node, 2);

        verify(versionManager).getVersionHistory(node);
    }

    @Test
    void removeVersionWithUnsupportedRepositoryOperationException() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistoryWithNames(node, VERSION_NAME);
        doThrow(new UnsupportedRepositoryOperationException("Unversionable")).when(versionHistory).removeVersion(VERSION_NAME);

        _versionPruneSubApp.removeVersion(node, versionHistory.getAllVersions(), versionHistory);

        verify(versionHistory).removeVersion(VERSION_NAME);
    }

    @Test
    void removeVersionWithReferentialIntegrityException() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        Version version = mockVersion(VERSION_NAME);
        VersionHistory versionHistory = mockVersionHistory(node, version);
        PropertyIterator propertyIterator = mock(PropertyIterator.class);
        when(version.getReferences()).thenReturn(propertyIterator);
        when(propertyIterator.toString()).thenReturn("references");
        doThrow(new ReferentialIntegrityException("Referenced")).when(versionHistory).removeVersion(VERSION_NAME);

        _versionPruneSubApp.removeVersion(node, versionHistory.getAllVersions(), versionHistory);

        verify(versionHistory).removeVersion(VERSION_NAME);
    }

    @Test
    void removeVersionWithGenericException() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistoryWithNames(node, VERSION_NAME);
        doThrow(new RepositoryException("Generic error")).when(versionHistory).removeVersion(VERSION_NAME);

        _versionPruneSubApp.removeVersion(node, versionHistory.getAllVersions(), versionHistory);

        verify(versionHistory).removeVersion(VERSION_NAME);
    }

    @Test
    void removeVersionSuccessfully() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistoryWithNames(node, VERSION_NAME);

        _versionPruneSubApp.removeVersion(node, versionHistory.getAllVersions(), versionHistory);

        verify(versionHistory).removeVersion(VERSION_NAME);
    }

    @Test
    void getIndexToRemoveWithZeroVersionsToKeep() {
        VersionIterator versionIterator = mock(VersionIterator.class);
        when(versionIterator.getSize()).thenReturn(5L);

        long result = _versionPruneSubApp.getIndexToRemove(versionIterator, 0);

        assertEquals(3L, result);
    }

    @Test
    void getIndexToRemoveWithPositiveVersionsToKeep() {
        VersionIterator versionIterator = mock(VersionIterator.class);
        when(versionIterator.getSize()).thenReturn(10L);

        long result = _versionPruneSubApp.getIndexToRemove(versionIterator, 3);

        assertEquals(6L, result);
    }

    @Test
    void getIndexToRemoveWithMoreVersionsToKeepThanExist() {
        VersionIterator versionIterator = mock(VersionIterator.class);
        when(versionIterator.getSize()).thenReturn(3L);

        long result = _versionPruneSubApp.getIndexToRemove(versionIterator, 10);

        assertEquals(-8L, result);
    }

    @Test
    void getVersionHistorySuccessfully() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistory(node);

        VersionHistory result = _versionPruneSubApp.getVersionHistory(node);

        assertNotNull(result);
        assertEquals(versionHistory, result);
    }

    @Test
    void getVersionHistoryWithException() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        doThrow(new RepositoryException()).when(node).getIdentifier();
        ComponentsMockUtils.mockComponentInstance(VersionManager.class);
        VersionHistory result = _versionPruneSubApp.getVersionHistory(node);

        assertNull(result);
    }

    @Test
    void getAllVersionsSuccessfully() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistory(node);
        VersionIterator result = _versionPruneSubApp.getAllVersions(node, versionHistory);

        assertNotNull(result);
        assertEquals(0L, result.getSize());
    }

    @Test
    void getAllVersionsWithException() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mock(VersionHistory.class);

        when(versionHistory.getAllVersions()).thenThrow(new RepositoryException("Error"));

        VersionIterator result = _versionPruneSubApp.getAllVersions(node, versionHistory);

        assertNull(result);
    }

    @Test
    void getVersionNameSuccessfully() throws Exception {
        Version version = mock(Version.class);
        when(version.getName()).thenReturn(VERSION_NAME);

        String result = _versionPruneSubApp.getVersionName(version);

        assertEquals(VERSION_NAME, result);
    }

    @Test
    void getVersionNameWithNullVersion() {
        String result = _versionPruneSubApp.getVersionName(null);

        assertEquals("", result);
    }

    @Test
    void getVersionNameWithException() throws Exception {
        Version version = mock(Version.class);
        when(version.getName()).thenThrow(new RepositoryException("Error"));

        String result = _versionPruneSubApp.getVersionName(version);

        assertEquals("", result);
    }

    @Test
    void getReferencesSuccessfully() throws Exception {
        Version version = mock(Version.class);
        PropertyIterator propertyIterator = mock(PropertyIterator.class);
        when(version.getReferences()).thenReturn(propertyIterator);
        when(propertyIterator.toString()).thenReturn("references");

        String result = _versionPruneSubApp.getReferences(version);

        assertEquals("references", result);
    }

    @Test
    void getReferencesWithNullVersion() {
        String result = _versionPruneSubApp.getReferences(null);

        assertEquals("", result);
    }

    @Test
    void getReferencesWithNullPropertyIterator() throws Exception {
        Version version = mock(Version.class);
        when(version.getReferences()).thenReturn(null);

        String result = _versionPruneSubApp.getReferences(version);

        assertEquals("", result);
    }

    @Test
    void getReferencesWithException() throws Exception {
        Version version = mock(Version.class);
        when(version.getReferences()).thenThrow(new RepositoryException("Error"));

        String result = _versionPruneSubApp.getReferences(version);

        assertEquals("", result);
    }

    @Test
    void getNodeSuccessfully() throws Exception {
        Node node = MagnoliaNodeMockUtils.mockPageNode(NODE_PATH);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);

        Node result = _versionPruneSubApp.getNode(NODE_PATH, WORKSPACE);

        assertNotNull(result);
        assertEquals(node, result);
    }

    @Test
    void getNodeWithException() throws Exception {
        when(_context.getJCRSession(WORKSPACE)).thenThrow(new RepositoryException("Error"));

        Node result = _versionPruneSubApp.getNode(NODE_PATH, WORKSPACE);

        assertNull(result);
    }

    @Test
    void doActionWithSuccessfulPruning() throws Exception {
        mockItemProperties(NODE_PATH, "1");

        Node node = MagnoliaNodeMockUtils.mockPageNode(NODE_PATH);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);

        VersionHistory versionHistory = mockVersionHistoryWithNames(node, "1.0", "1.1", "1.2", "1.3", "1.4");

        when(_simpleTranslator.translate("versionPrune.pruned")).thenReturn("versions pruned");

        _versionPruneSubApp.doAction();

        verify(versionHistory, times(3)).removeVersion(anyString());
        verify(_view).buildResultView(anyString());
    }

    @Test
    void handleNodeWithNullAllVersions() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistory(node);
        when(versionHistory.getAllVersions()).thenThrow(new RepositoryException("Error"));

        _versionPruneSubApp.handleNode(node, 2);

        verify(versionHistory, never()).removeVersion(anyString());
    }

    @Test
    void handleNodeWithZeroIndexToRemove() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistoryWithNames(node, "1.0", "1.1", "1.2");

        _versionPruneSubApp.handleNode(node, 10);

        verify(versionHistory, never()).removeVersion(anyString());
    }

    @Test
    void doActionWithVersionsZero() throws Exception {
        mockItemProperties(NODE_PATH, "0");

        Node node = MagnoliaNodeMockUtils.mockPageNode("/node");
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);
        mockVersionHistoryWithNames(node, "1.0", "1.1", "1.2");

        when(_simpleTranslator.translate("versionPrune.nothingPruned")).thenReturn("Nothing pruned");

        _versionPruneSubApp.doAction();

        verify(_view).buildResultView(anyString());
    }

    @Test
    void handleNodeWithDepthZero() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, "/");
        VersionManager versionManager = ComponentsMockUtils.mockComponentInstance(VersionManager.class);

        _versionPruneSubApp.handleNode(node, 2);

        verify(versionManager).getVersionHistory(node);
    }

    @Test
    void handleNodeWithNegativeIndexToRemove() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);

        VersionHistory versionHistory = mockVersionHistory(node);
        _versionPruneSubApp.handleNode(node, 5);

        verify(versionHistory, never()).removeVersion(anyString());
    }

    @Test
    void getNodeWithSessionException() throws Exception {
        when(_context.getJCRSession(WORKSPACE)).thenThrow(new RepositoryException("Session error"));

        Node result = _versionPruneSubApp.getNode(NODE_PATH, WORKSPACE);

        assertNull(result);
    }

    @Test
    void removeVersionMultipleTimes() throws Exception {
        Node node = NodeMockUtils.mockNode(WORKSPACE, NODE_PATH);
        VersionHistory versionHistory = mockVersionHistoryWithNames(node, "1.0", "1.1");

        _versionPruneSubApp.removeVersion(node, versionHistory.getAllVersions(), versionHistory);
        _versionPruneSubApp.removeVersion(node, versionHistory.getAllVersions(), versionHistory);

        verify(versionHistory, times(2)).removeVersion(anyString());
    }

    void mockItemProperties(String path, String versions) {
        Property pathProperty = mock(Property.class);
        Property workspaceProperty = mock(Property.class);
        Property versionsProperty = mock(Property.class);

        when(_item.getItemProperty("path")).thenReturn(pathProperty);
        when(_item.getItemProperty("workspace")).thenReturn(workspaceProperty);
        when(_item.getItemProperty("versions")).thenReturn(versionsProperty);

        when(pathProperty.getValue()).thenReturn(path);
        when(workspaceProperty.getValue()).thenReturn(VersionPruneSubAppTest.WORKSPACE);
        when(versionsProperty.getValue()).thenReturn(versions);
    }

    VersionHistory mockVersionHistoryWithNames(Node node, String... versions) {
        Version[] versionMocks = Arrays.stream(versions).map(this::mockVersion).toArray(Version[]::new);
        try {
            return mockVersionHistory(node, versionMocks);
        } catch (RepositoryException e) {
            // Ignore while mocking
            return null;
        }
    }

    VersionHistory mockVersionHistory(Node node, Version... versions) throws RepositoryException {
        VersionManager versionManager = ComponentsMockUtils.mockComponentInstance(VersionManager.class);
        VersionHistory versionHistory = mock(VersionHistory.class);
        when(versionManager.getVersionHistory(node)).thenReturn(versionHistory);
        VersionIterator versionIterator = new VersionIteratorAdapter(Arrays.asList(versions));
        when(versionHistory.getAllVersions()).thenReturn(versionIterator);
        return versionHistory;
    }

    Version mockVersion(String name) {
        Version version = mock(Version.class);
        try {
            when(version.getName()).thenReturn(name);
        } catch (RepositoryException e) {
            // Ignore while mocking
        }
        return version;
    }
}

