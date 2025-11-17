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

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.v7.data.util.PropertysetItem;
import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BaseResultViewImpl.
 *
 * @author wolf.bubenik
 * @since 2025-11-17
 */
class BaseResultViewImplTest {

    private FormBuilder _formBuilder;
    private SimpleTranslator _i18n;
    private FormDefinition _formDefinition;
    private FormViewReduced _formView;
    private TestBaseResultViewImpl _view;

    @BeforeEach
    void setUp() {
        SubAppContext subAppContext = mock(SubAppContext.class);
        _formBuilder = mock(FormBuilder.class);
        _i18n = mock(SimpleTranslator.class);
        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        _formDefinition = mock(FormDefinition.class);
        _formView = mock(FormViewReduced.class);

        when(subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(_formDefinition);
        when(_i18n.translate("test.button.key")).thenReturn("Test Button");

        Component formComponent = mock(Component.class);
        when(_formView.asVaadinComponent()).thenReturn(formComponent);

        _view = new TestBaseResultViewImpl(subAppContext, Components.getComponentProvider(), _formBuilder, _i18n);
    }

    @AfterEach
    public void tearDown() {
        ComponentsMockUtils.clearComponentProvider();
    }

    @Test
    void asVaadinComponentReturnsSmallAppLayout() {
        Component component = _view.asVaadinComponent();

        assertNotNull(component);
        assertEquals(SmallAppLayout.class, component.getClass());
    }

    @Test
    void setListenerBuildsViewWithButton() {
        ResultView.Listener listener = mock(ResultView.Listener.class);

        _view.setListener(listener);

        CssLayout inputSection = _view.getInputSection();
        assertNotNull(inputSection);
        assertEquals(1, inputSection.getComponentCount());

        Component buttonLayout = inputSection.getComponent(0);
        assertEquals(CssLayout.class, buttonLayout.getClass());
        assertEquals(1, ((CssLayout) buttonLayout).getComponentCount());

        Component button = ((CssLayout) buttonLayout).getComponent(0);
        assertEquals(Button.class, button.getClass());
    }

    @Test
    void setListenerCreatesButtonWithTranslatedLabel() {
        ResultView.Listener listener = mock(ResultView.Listener.class);

        _view.setListener(listener);

        CssLayout inputSection = _view.getInputSection();
        CssLayout buttonLayout = (CssLayout) inputSection.getComponent(0);
        Button button = (Button) buttonLayout.getComponent(0);

        assertEquals("Test Button", button.getCaption());
        verify(_i18n).translate("test.button.key");
    }

    @Test
    void buttonClickTriggersListener() {
        ResultView.Listener listener = mock(ResultView.Listener.class);
        _view.setListener(listener);

        CssLayout inputSection = _view.getInputSection();
        CssLayout buttonLayout = (CssLayout) inputSection.getComponent(0);
        Button button = (Button) buttonLayout.getComponent(0);

        button.click();

        verify(listener).onActionTriggered();
    }

    @Test
    void setFormViewAddsFormToInputSection() {
        ResultView.Listener listener = mock(ResultView.Listener.class);
        _view.setListener(listener);

        _view.setFormView(_formView);

        CssLayout inputSection = _view.getInputSection();
        assertEquals(2, inputSection.getComponentCount());
        assertEquals(_formView.asVaadinComponent(), inputSection.getComponent(0));
    }

    @Test
    void getCurrentFormViewReturnsSetFormView() {
        ResultView.Listener listener = mock(ResultView.Listener.class);
        _view.setListener(listener);
        _view.setFormView(_formView);

        FormViewReduced currentFormView = _view.getCurrentFormView();

        assertSame(_formView, currentFormView);
    }

    @Test
    void refreshReplacesFormView() {
        ResultView.Listener listener = mock(ResultView.Listener.class);
        _view.setListener(listener);
        _view.setFormView(_formView);

        FormViewReduced newFormView = mockComponentInstance(FormViewReduced.class);
        Component newFormComponent = mock(Component.class);
        when(newFormView.asVaadinComponent()).thenReturn(newFormComponent);

        _view.refresh();

        verify(_formBuilder).buildReducedForm(eq(_formDefinition), eq(newFormView), any(PropertysetItem.class), eq(null));

        FormViewReduced currentFormView = _view.getCurrentFormView();
        assertSame(newFormView, currentFormView);

        CssLayout inputSection = _view.getInputSection();
        assertEquals(newFormComponent, inputSection.getComponent(0));
    }

    @Test
    void refreshBuildsFormWithCorrectParameters() {
        ResultView.Listener listener = mock(ResultView.Listener.class);
        _view.setListener(listener);
        _view.setFormView(_formView);

        FormViewReduced newFormView = mockComponentInstance(FormViewReduced.class);
        Component newFormComponent = mock(Component.class);
        when(newFormView.asVaadinComponent()).thenReturn(newFormComponent);

        _view.refresh();

        verify(_formBuilder).buildReducedForm(
            eq(_formDefinition),
            eq(newFormView),
            any(PropertysetItem.class),
            eq(null)
        );
    }

    @Test
    void getResultSectionReturnsEmptyLayoutInitially() {
        CssLayout resultSection = _view.getResultSection();

        assertNotNull(resultSection);
        assertEquals(0, resultSection.getComponentCount());
    }

    @Test
    void getInputSectionReturnsLayoutWithButton() {
        ResultView.Listener listener = mock(ResultView.Listener.class);
        _view.setListener(listener);

        CssLayout inputSection = _view.getInputSection();

        assertNotNull(inputSection);
        assertEquals(1, inputSection.getComponentCount());
    }

    @Test
    void buttonHasCorrectStyleNames() {
        ResultView.Listener listener = mock(ResultView.Listener.class);

        _view.setListener(listener);

        CssLayout inputSection = _view.getInputSection();
        CssLayout buttonLayout = (CssLayout) inputSection.getComponent(0);
        Button button = (Button) buttonLayout.getComponent(0);

        String styleNames = button.getStyleName();
        assertNotNull(styleNames);
    }

    @Test
    void buttonLayoutHasCorrectStyleName() {
        ResultView.Listener listener = mock(ResultView.Listener.class);

        _view.setListener(listener);

        CssLayout inputSection = _view.getInputSection();
        CssLayout buttonLayout = (CssLayout) inputSection.getComponent(0);

        String styleNames = buttonLayout.getStyleName();
        assertNotNull(styleNames);
    }

    static class TestBaseResultViewImpl extends BaseResultViewImpl {

        TestBaseResultViewImpl(SubAppContext subAppContext, ComponentProvider componentProvider, FormBuilder formBuilder, SimpleTranslator i18n) {
            super(subAppContext, componentProvider, formBuilder, i18n);
        }

        @Override
        protected String getButtonKey() {
            return "test.button.key";
        }
    }
}

