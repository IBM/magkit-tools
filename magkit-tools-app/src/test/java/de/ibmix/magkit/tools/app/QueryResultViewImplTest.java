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

import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.CssLayout;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.jcr.RowStubbingOperation.stubPath;
import static de.ibmix.magkit.test.jcr.RowStubbingOperation.stubValue;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockRow;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockRowQueryResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for QueryResultViewImpl.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-18
 */
class QueryResultViewImplTest {

    private QueryResultViewImpl _view;

    @BeforeEach
    void setUp() throws Exception {
        mockWebContext(stubContextPath("/test"));

        SubAppContext subAppContext = mock(SubAppContext.class);
        ComponentProvider componentProvider = Components.getComponentProvider();
        FormBuilder formBuilder = mock(FormBuilder.class);
        SimpleTranslator i18n = mock(SimpleTranslator.class);

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        FormDefinition formDefinition = mock(FormDefinition.class);

        when(subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(formDefinition);
        when(i18n.translate("queryTools.button.label")).thenReturn("Execute Query");

        _view = new QueryResultViewImpl(subAppContext, componentProvider, formBuilder, i18n);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void constructorInitializesResultSectionWithBrowserFrame() {
        CssLayout resultSection = _view.getResultSection();

        assertNotNull(resultSection);
        assertEquals(1, resultSection.getComponentCount());
        assertEquals(BrowserFrame.class, resultSection.getComponent(0).getClass());
    }

    @Test
    void browserFrameHasCorrectProperties() {
        CssLayout resultSection = _view.getResultSection();
        BrowserFrame browserFrame = (BrowserFrame) resultSection.getComponent(0);

        assertNotNull(browserFrame);
        assertEquals("Tipps zu JCR-SQL2", browserFrame.getCaption());
        assertEquals("100.0%", browserFrame.getWidth() + browserFrame.getWidthUnits().getSymbol());
        assertEquals("640.0px", browserFrame.getHeight() + browserFrame.getHeightUnits().getSymbol());
        assertTrue(browserFrame.getStyleName().contains("help-frame"));
    }

    @Test
    void getButtonKeyReturnsCorrectKey() {
        String buttonKey = _view.getButtonKey();

        assertEquals("queryTools.button.label", buttonKey);
    }

    @Test
    void implementsCorrectInterfaces() {
        assertInstanceOf(BaseResultViewImpl.class, _view);
        assertInstanceOf(QueryResultView.class, _view);
    }

    @Test
    void buildResultTableAddsTableToResultSection() throws RepositoryException {
        // Given
        CssLayout resultSection = _view.getResultSection();
        // Initially only the BrowserFrame
        assertEquals(1, resultSection.getComponentCount());

        // When
        _view.buildResultTable(null, true, true, 100);

        // Then
        assertEquals(2, resultSection.getComponentCount());
        assertInstanceOf(QueryResultTable.class, resultSection.getComponent(0));
        QueryResultTable resultTable = (QueryResultTable) resultSection.getComponent(0);
        assertEquals(0, resultTable.size());

        // When we have a query result table already
        QueryResult queryResult = mockRowQueryResult(
            mockRow(0.5, stubPath("mgnl:page", "/test/path"), stubValue("jcr:title", "Test Title"), stubValue("jcr:uuid", "test-uuid"))
        );
        String[] selectorNames = new String[]{"mgnl:page"};
        when(queryResult.getSelectorNames()).thenReturn(selectorNames);
        String[] columnNames = new String[]{"jcr:title", "jcr:uuid"};
        when(queryResult.getColumnNames()).thenReturn(columnNames);

        _view.buildResultTable(queryResult, true, true, 200);

        // Then
        assertEquals(2, resultSection.getComponentCount());
        resultTable = (QueryResultTable) resultSection.getComponent(0);
        assertEquals(1, resultTable.size());
    }
}

