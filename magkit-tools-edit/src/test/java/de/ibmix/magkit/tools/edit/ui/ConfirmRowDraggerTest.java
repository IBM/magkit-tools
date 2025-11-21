package de.ibmix.magkit.tools.edit.ui;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
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

import com.vaadin.shared.ui.grid.DropLocation;
import com.vaadin.shared.ui.grid.DropMode;
import com.vaadin.ui.Grid;
import de.ibmix.magkit.tools.edit.setup.EditToolsModule;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.browser.drop.DropConstraint;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrSessionWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.inject.Provider;
import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Workspace;
import javax.jcr.Node;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.WorkspaceMockUtils.mockWorkspace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ConfirmRowDragger} covering confirmation content generation, title creation, move execution and confirmation decision logic.
 *
 * @author wolf.bubenik
 * @since 2025-11-19
 */
public class ConfirmRowDraggerTest {

    private ConfirmRowDragger<Item> _dragger;
    private Datasource<Item> _datasource;
    private Grid<Item> _grid;
    private Provider<EditToolsModule> _moduleProvider;

    /**
     * Sets up a test instance with mocked collaborators.
     */
    @BeforeEach
    public void setUp() {
        _datasource = mock(Datasource.class);
        _grid = new Grid<>();
        DropConstraint<Item> dropConstraint = mock(DropConstraint.class);
        when(dropConstraint.isAllowedAt(anyCollection(), any(), any())).thenReturn(true);
        SimpleTranslator translator = mock(SimpleTranslator.class);
        when(translator.translate("magkit.moveItem.confirmationQuestionOneItem")).thenReturn("one");
        when(translator.translate("magkit.moveItem.confirmationQuestionManyItems", 3)).thenReturn("many(3)");
        _moduleProvider = mock(Provider.class);
        when(_moduleProvider.get()).thenReturn(new EditToolsModule());
        _dragger = new ConfirmRowDragger<>(_datasource, _grid, DropMode.BETWEEN, dropConstraint, translator, _moduleProvider);
    }

    /**
     * Verifies that confirmation content lists provided item paths.
     */
    @Test
    public void testCreateConfirmContentListsPaths() throws RepositoryException {
        Node item1 = mockNode("website", "/path/one");
        Node item2 = mockNode("website", "/path/two");
        String html = _dragger.createConfirmContent(Arrays.asList(item1, item2));
        assertTrue(html.startsWith("<ul>"));
        assertTrue(html.contains("<li>/path/one</li>"));
        assertTrue(html.contains("<li>/path/two</li>"));
        assertTrue(html.endsWith("</ul>"));
    }

    /**
     * Verifies content generation skips items throwing repository exceptions.
     */
    @Test
    public void testCreateConfirmContentHandlesRepositoryException() throws Exception {
        Item faulty = mock(Item.class);
        when(faulty.getPath()).thenThrow(new RepositoryException("fail"));
        Node ok = mockNode("website", "/path/ok");
        String html = _dragger.createConfirmContent(Arrays.asList(faulty, ok));
        assertTrue(html.contains("/path/ok"));
        assertFalse(html.contains("fail"));
    }

    /**
     * Verifies title creation for a single item selection.
     */
    @Test
    public void testCreateConfirmTitleSingle() throws RepositoryException {
        Node item = mockNode("website", "/path/only");
        String title = _dragger.createConfirmTitle(List.of(item));
        assertEquals("one", title);
    }

    /**
     * Verifies title creation for multiple items selection.
     */
    @Test
    public void testCreateConfirmTitleMany() throws RepositoryException {
        Node item1 = mockNode("website", "/path/one");
        Node item2 = mockNode("website", "/path/two");
        Node item3 = mockNode("website", "/path/three");
        String title = _dragger.createConfirmTitle(Arrays.asList(item1, item2, item3));
        assertEquals("many(3)", title);
    }

    /**
     * Verifies that move execution delegates to datasource and refreshes the grid.
     */
    @Test
    public void testDoMoveItemsDelegatesToDatasource() throws Exception {
        Collection<Item> items = Arrays.asList(mockNode("website", "/path/one"));
        Item target = mockNode("website", "/path/target");
        setPrivateField("_target", target);
        setPrivateField("_dropLocation", DropLocation.BELOW);
        _dragger.doMoveItems(_datasource, _grid, items);
        ArgumentCaptor<Collection> captor = ArgumentCaptor.forClass(Collection.class);
        verify(_datasource).moveItems(captor.capture(), eq(target), eq(DropLocation.BELOW));
        assertEquals(1, captor.getValue().size());
    }

    /**
     * Verifies confirmation decision returns true for configured JCR workspace.
     */
    @Test
    public void testShowConfirmationEnabledWorkspace() throws RepositoryException {
        EditToolsModule module = new EditToolsModule();
        module.setMoveConfirmWorkspaces(List.of("website"));
        when(_moduleProvider.get()).thenReturn(module);
        JcrDatasource jcrDatasource = mockJcrDatasource("website");
        boolean show = _dragger.showConfirmation(jcrDatasource);
        assertTrue(show);
    }

    /**
     * Verifies confirmation decision returns false for non JCR datasource.
     */
    @Test
    public void testShowConfirmationNonJcrDatasource() {
        boolean show = _dragger.showConfirmation(_datasource);
        assertFalse(show);
    }

    /**
     * Verifies confirmation decision returns false when workspace not configured.
     */
    @Test
    public void testShowConfirmationWorkspaceNotConfigured() throws RepositoryException {
        EditToolsModule module = new EditToolsModule();
        module.setMoveConfirmWorkspaces(List.of("other"));
        when(_moduleProvider.get()).thenReturn(module);
        JcrDatasource jcrDatasource = mockJcrDatasource("website");
        boolean show = _dragger.showConfirmation(jcrDatasource);
        assertFalse(show);
    }

    /**
     * Verifies confirmation decision handles repository exception gracefully.
     */
    @Test
    public void testShowConfirmationHandlesRepositoryException() throws RepositoryException {
        EditToolsModule module = new EditToolsModule();
        module.setMoveConfirmWorkspaces(List.of("website"));
        when(_moduleProvider.get()).thenReturn(module);
        JcrDatasource jcrDatasource = mock(JcrDatasource.class);
        when(jcrDatasource.getJCRSession()).thenThrow(new RepositoryException("fail"));
        boolean show = _dragger.showConfirmation(jcrDatasource);
        assertFalse(show);
    }

    /**
     * Verifies confirm content for empty collection returns just ul wrapper.
     */
    @Test
    public void testCreateConfirmContentEmpty() {
        String html = _dragger.createConfirmContent(List.of());
        assertEquals("<ul></ul>", html);
    }

    /**
     * Verifies showConfirmation returns false when module workspace list is empty (default state).
     */
    @Test
    public void testShowConfirmationEmptyWorkspaceList() throws RepositoryException {
        EditToolsModule module = new EditToolsModule();
        when(_moduleProvider.get()).thenReturn(module);
        JcrDatasource jcrDatasource = mock(JcrDatasource.class);
        JcrSessionWrapper session = mock(JcrSessionWrapper.class);
        Workspace workspace = mockWorkspace("website");
        when(session.getWorkspace()).thenReturn(workspace);
        when(jcrDatasource.getJCRSession()).thenReturn(session);
        boolean show = _dragger.showConfirmation(jcrDatasource);
        assertFalse(show);
    }

    /**
     * Verifies title generation for empty list (edge case) falls back to single item translation.
     */
    @Test
    public void testCreateConfirmTitleEmptyList() {
        String title = _dragger.createConfirmTitle(List.of());
        assertEquals("one", title);
    }

    /**
     * Sets a private field value via reflection to prepare internal state for testing.
     *
     * @param name the field name
     * @param value the value to set
     * @throws Exception if reflection access fails
     */
    private void setPrivateField(String name, Object value) throws Exception {
        Field field = ConfirmRowDragger.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(_dragger, value);
    }

    JcrDatasource mockJcrDatasource(String workspaceName) throws RepositoryException {
        JcrDatasource jcrDatasource = mock(JcrDatasource.class);
        JcrSessionWrapper session = mock(JcrSessionWrapper.class);
        Workspace workspace = mockWorkspace(workspaceName);
        when(session.getWorkspace()).thenReturn(workspace);
        when(jcrDatasource.getJCRSession()).thenReturn(session);
        return jcrDatasource;
    }
}
