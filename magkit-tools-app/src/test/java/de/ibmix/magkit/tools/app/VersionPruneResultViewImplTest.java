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

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for VersionPruneResultViewImpl.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class VersionPruneResultViewImplTest {

    private SimpleTranslator _i18n;
    private VersionPruneResultViewImpl _view;

    @BeforeEach
    void setUp() {
        SubAppContext subAppContext = mock(SubAppContext.class);
        ComponentProvider componentProvider = Components.getComponentProvider();
        FormBuilder formBuilder = mock(FormBuilder.class);
        _i18n = mock(SimpleTranslator.class);

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        @SuppressWarnings("deprecation")
        FormDefinition formDefinition = mock(FormDefinition.class);

        when(subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(formDefinition);
        when(_i18n.translate("versionPrune.description")).thenReturn("Version Pruning Description");
        when(_i18n.translate("versionPrune.submitPrune")).thenReturn("Prune Versions");

        _view = new VersionPruneResultViewImpl(subAppContext, componentProvider, formBuilder, _i18n);
    }

    @AfterEach
    void tearDown() {
        ComponentsMockUtils.clearComponentProvider();
    }

    @Test
    void constructorCreatesViewWithDescriptionLabel() {
        CssLayout inputSection = _view.getInputSection();

        assertNotNull(inputSection);
        assertEquals(1, inputSection.getComponentCount());

        Component firstComponent = inputSection.getComponent(0);
        assertInstanceOf(Label.class, firstComponent);

        Label descriptionLabel = (Label) firstComponent;
        assertEquals("Version Pruning Description", descriptionLabel.getValue());
        assertTrue(descriptionLabel.getStyleName().contains("prune-hint"));

        verify(_i18n).translate("versionPrune.description");
    }

    @Test
    void constructorCreatesTextAreaInResultSection() {
        CssLayout resultSection = _view.getResultSection();

        assertNotNull(resultSection);
        assertEquals(1, resultSection.getComponentCount());

        Component firstComponent = resultSection.getComponent(0);
        assertInstanceOf(TextArea.class, firstComponent);

        TextArea textArea = (TextArea) firstComponent;
        assertEquals(20, textArea.getRows());
        assertEquals("100.0%", textArea.getWidth() + textArea.getWidthUnits().getSymbol());
    }

    @Test
    void getButtonKeyReturnsCorrectI18nKey() {
        String buttonKey = _view.getButtonKey();

        assertEquals("versionPrune.submitPrune", buttonKey);
    }

    @Test
    void buildResultViewSetsValueInTextArea() {
        String testResult = "Pruned 10 versions from node /content/test";

        _view.buildResultView(testResult);

        CssLayout resultSection = _view.getResultSection();
        TextArea textArea = (TextArea) resultSection.getComponent(0);

        assertEquals(testResult, textArea.getValue());
        assertTrue(textArea.isReadOnly());
    }

    @Test
    void buildResultViewWithEmptyString() {
        _view.buildResultView("");

        CssLayout resultSection = _view.getResultSection();
        TextArea textArea = (TextArea) resultSection.getComponent(0);

        assertEquals("", textArea.getValue());
        assertTrue(textArea.isReadOnly());
    }

    @Test
    void buildResultViewWithNullValue() {
        _view.buildResultView(null);

        CssLayout resultSection = _view.getResultSection();
        TextArea textArea = (TextArea) resultSection.getComponent(0);

        assertEquals("", textArea.getValue());
        assertTrue(textArea.isReadOnly());
    }

    @Test
    void buildResultViewWithMultiLineText() {
        String multiLineResult = "Pruned versions:\n/content/node1: 5 versions\n/content/node2: 3 versions\nTotal: 8 versions";

        _view.buildResultView(multiLineResult);

        CssLayout resultSection = _view.getResultSection();
        TextArea textArea = (TextArea) resultSection.getComponent(0);

        assertEquals(multiLineResult, textArea.getValue());
        assertTrue(textArea.isReadOnly());
    }

    @Test
    void buildResultViewMultipleTimesUpdatesTextArea() {
        String firstResult = "First pruning: 5 versions";
        String secondResult = "Second pruning: 10 versions";

        _view.buildResultView(firstResult);

        CssLayout resultSection = _view.getResultSection();
        TextArea textArea = (TextArea) resultSection.getComponent(0);
        assertEquals(firstResult, textArea.getValue());

        _view.buildResultView(secondResult);

        assertEquals(secondResult, textArea.getValue());
        assertTrue(textArea.isReadOnly());
    }

    @Test
    void buildResultViewSetsReadOnlyFalseBeforeUpdatingValue() {
        String testResult = "Test result";

        CssLayout resultSection = _view.getResultSection();
        TextArea textArea = (TextArea) resultSection.getComponent(0);

        assertFalse(textArea.isReadOnly());

        _view.buildResultView(testResult);

        assertEquals(testResult, textArea.getValue());
        assertTrue(textArea.isReadOnly());
    }

    @Test
    void textAreaIsInitiallyNotReadOnly() {
        CssLayout resultSection = _view.getResultSection();
        TextArea textArea = (TextArea) resultSection.getComponent(0);

        assertFalse(textArea.isReadOnly());
        assertEquals("", textArea.getValue());
    }
}

