package de.ibmix.magkit.tools.t9n;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Translation
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

import info.magnolia.cms.security.User;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.observation.DatasourceObservation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Optional;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubUser;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockUser;
import static de.ibmix.magkit.test.cms.security.UserStubbingOperation.stubLanguage;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TranslationSaveFormAction} covering node rename and commit scenarios.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-20
 */
public class TranslationSaveFormActionTest {
    private CommitActionDefinition _definition;
    private CloseHandler _closeHandler;
    private ValueContext<Node> _valueContext;
    private FormView<Node> _formView;
    private Datasource<Node> _datasource;
    private DatasourceObservation.Manual _datasourceObservation;
    private NodeNameHelper _nodeNameHelper;

    @BeforeEach
    public void setUp() throws Exception {
        _definition = mock(CommitActionDefinition.class);
        _closeHandler = mock(CloseHandler.class);
        _valueContext = mock(ValueContext.class);
        _formView = mock(FormView.class);
        _datasource = mock(Datasource.class);
        _datasourceObservation = mock(DatasourceObservation.Manual.class);
        _nodeNameHelper = mockComponentInstance(NodeNameHelper.class);
        when(_nodeNameHelper.getValidatedName(anyString())).thenAnswer(inv -> inv.getArguments()[0]);
        when(_nodeNameHelper.getUniqueName(any(Node.class), anyString())).thenAnswer(inv -> inv.getArguments()[1]);
        final User user = mockUser("Paul", stubLanguage("en"));
        mockWebContext(stubUser(user));
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    /**
     * Verifies NodeNameHelper is invoked with validated and unique name for rename.
     */
    @Test
    public void nodeNameHelperInvokedForRename() throws RepositoryException {
        Node parent = mockNode("translation", "/parent");
        Node node = mockNode("translation", "/parent/tmp", stubProperty("key", "someKey"));
        when(node.isNew()).thenReturn(true);
        when(_valueContext.getSingle()).thenReturn(Optional.of(node));
        TranslationSaveFormAction action = new TranslationSaveFormAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _nodeNameHelper);
        action.write();
        verify(_formView).write(node);
        verify(_datasource).commit(node);
        verify(_datasourceObservation).trigger();
        verify(_nodeNameHelper).getValidatedName("someKey");
        verify(_nodeNameHelper).getUniqueName(parent, "someKey");
        verify(node.getSession()).move("/parent/tmp", "/parent/someKey");
        verify(node.getSession(), never()).move(eq("/parent"), anyString());
    }

    /**
     * Verifies that a new node without key property is not renamed but committed.
     */
    @Test
    public void writeDoesNotRenameWithoutKey() throws RepositoryException {
        Node node = mockNode("translation", "/parent/tmp");
        when(node.isNew()).thenReturn(true);
        when(_valueContext.getSingle()).thenReturn(Optional.of(node));
        TranslationSaveFormAction action = new TranslationSaveFormAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _nodeNameHelper);
        action.write();
        verify(_formView).write(node);
        verify(_datasource).commit(node);
        verify(_datasourceObservation).trigger();
        verify(_nodeNameHelper, never()).getUniqueName(any(Node.class), anyString());
        verify(node.getSession(), never()).move(anyString(), anyString());
    }

    /**
     * Verifies that an existing node (isNew false) is not renamed even if key property exists.
     */
    @Test
    public void writeDoesNotRenameExistingNode() throws RepositoryException {
        Node node = mockNode("translation", "/parent/existing", stubProperty("key", "existingKey"));
        when(node.isNew()).thenReturn(false);
        when(_valueContext.getSingle()).thenReturn(Optional.of(node));
        TranslationSaveFormAction action = new TranslationSaveFormAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _nodeNameHelper);
        action.write();
        verify(_formView).write(node);
        verify(_datasource).commit(node);
        verify(_datasourceObservation).trigger();
        verify(_nodeNameHelper, never()).getUniqueName(any(Node.class), anyString());
        verify(node.getSession(), never()).move(anyString(), anyString());
    }

    /**
     * Verifies that write does nothing when ValueContext is empty.
     */
    @Test
    public void writeDoesNothingWhenContextEmpty() {
        when(_valueContext.getSingle()).thenReturn(Optional.empty());
        TranslationSaveFormAction action = new TranslationSaveFormAction(_definition, _closeHandler, _valueContext, _formView, _datasource, _datasourceObservation, _nodeNameHelper);
        action.write();
        verify(_formView, never()).write(any());
        verify(_datasource, never()).commit(any());
        verify(_datasourceObservation, never()).trigger();
        verify(_nodeNameHelper, never()).getValidatedName(anyString());
    }
}
