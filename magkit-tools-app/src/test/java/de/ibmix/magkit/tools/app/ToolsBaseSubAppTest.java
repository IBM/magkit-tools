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

import com.vaadin.v7.data.util.PropertysetItem;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ToolsBaseSubApp.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-18
 */
class ToolsBaseSubAppTest {

    private SubAppContext _subAppContext;
    private FormViewReduced _formView;
    private ResultView _view;
    private FormBuilder _builder;
    private FormDefinition _formDefinition;
    private TestToolsSubApp _testSubApp;
    private boolean _doActionCalled;

    @BeforeEach
    void setUp() {
        ContextMockUtils.cleanContext();

        _subAppContext = mock(SubAppContext.class);
        _formView = mock(FormViewReduced.class);
        _view = mock(ResultView.class);
        _builder = mock(FormBuilder.class);
        _formDefinition = mock(FormDefinition.class);
        _doActionCalled = false;

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        when(_subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(_formDefinition);

        _testSubApp = new TestToolsSubApp(_subAppContext, _formView, _view, _builder);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void constructorInitializesFields() {
        assertNotNull(_testSubApp);
    }

    @Test
    void constructorStoresFormDefinitionFromDescriptor() {
        FormDefinition formDef = _testSubApp.getFormDefinition();
        assertNotNull(formDef);
        assertSame(_formDefinition, formDef);
    }

    @Test
    void onActionTriggeredValidatesForm() {
        when(_formView.isValid()).thenReturn(false);

        _testSubApp.onActionTriggered();

        verify(_formView).showValidation(true);
        assertFalse(_doActionCalled);
    }

    @Test
    void onActionTriggeredExecutesActionWhenFormIsValid() {
        when(_formView.isValid()).thenReturn(true);

        _testSubApp.onActionTriggered();

        verify(_formView).showValidation(true);
        verify(_formView).isValid();
        assertTrue(_doActionCalled);
    }

    @Test
    void onActionTriggeredDoesNotExecuteActionWhenFormIsInvalid() {
        when(_formView.isValid()).thenReturn(false);

        _testSubApp.onActionTriggered();

        verify(_formView).showValidation(true);
        verify(_formView).isValid();
        assertFalse(_doActionCalled);
    }

    @Test
    void onSubAppStartBuildsFormAndSetsUpView() {
        _testSubApp.onSubAppStart();

        verify(_builder).buildReducedForm(eq(_formDefinition), eq(_formView), any(PropertysetItem.class), isNull());
        verify(_view).setFormView(_formView);
        verify(_view).setListener(_testSubApp);
    }

    @Test
    void getFormDefinitionReturnsCorrectDefinition() {
        FormDefinition result = _testSubApp.getFormDefinition();

        assertSame(_formDefinition, result);
    }

    @Test
    void onActionTriggeredWithMultipleValidationCycles() {
        when(_formView.isValid()).thenReturn(false, false, true);

        _testSubApp.onActionTriggered();
        assertFalse(_doActionCalled);

        _testSubApp.onActionTriggered();
        assertFalse(_doActionCalled);

        _testSubApp.onActionTriggered();
        assertTrue(_doActionCalled);
    }

    @Test
    void constructorWithDifferentViewTypes() {
        ResultView customView = mock(ResultView.class);
        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        when(_subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(_formDefinition);

        TestToolsSubApp subApp = new TestToolsSubApp(_subAppContext, _formView, customView, _builder);

        assertNotNull(subApp);
        assertEquals(_formDefinition, subApp.getFormDefinition());
    }

    /**
     * Concrete test implementation of ToolsBaseSubApp for testing purposes.
     */
    private class TestToolsSubApp extends ToolsBaseSubApp<ResultView> {

        TestToolsSubApp(final SubAppContext subAppContext, final FormViewReduced formView, final ResultView view, final FormBuilder builder) {
            super(subAppContext, formView, view, builder);
        }

        @Override
        protected void doAction() {
            _doActionCalled = true;
        }
    }
}

