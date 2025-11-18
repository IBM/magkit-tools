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
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAttribute;
import static info.magnolia.context.Context.SESSION_SCOPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for QuerySubApp.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-18
 */
class QuerySubAppTest {

    private static final String WORKSPACE = "website";
    private static final String STATEMENT = "SELECT * FROM [nt:base]";
    private static final String QUERY_LANGUAGE = "JCR-SQL2";

    private SubAppContext _subAppContext;
    private FormViewReduced _formView;
    private QueryResultView _view;
    private FormBuilder _builder;
    private SimpleTranslator _simpleTranslator;
    private Provider<Context> _contextProvider;
    private Context _context;
    private QuerySubApp _querySubApp;
    private Item _item;

    @BeforeEach
    void setUp() throws Exception {
        mockWebContext();

        _subAppContext = mock(SubAppContext.class);
        _formView = mock(FormViewReduced.class);
        _view = mock(QueryResultView.class);
        _builder = mock(FormBuilder.class);
        _simpleTranslator = mock(SimpleTranslator.class);
        _contextProvider = mock(Provider.class);
        _context = mock(Context.class);
        _item = mock(Item.class);

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        FormDefinition formDefinition = mock(FormDefinition.class);

        when(_subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(formDefinition);
        when(_contextProvider.get()).thenReturn(_context);
        when(_formView.getItemDataSource()).thenReturn(_item);

        _querySubApp = new QuerySubApp(_subAppContext, _formView, _view, _builder, _simpleTranslator, _contextProvider);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void constructorInitializesFields() {
        assertInstanceOf(ToolsBaseSubApp.class, _querySubApp);
    }

    @Test
    void doActionExecutesQuerySuccessfully() throws RepositoryException {
        mockItemProperties(QUERY_LANGUAGE, WORKSPACE, STATEMENT, "true", "false");

        QueryResult queryResult = ContextMockUtils.mockQueryResult(WORKSPACE, QUERY_LANGUAGE, STATEMENT);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);
        when(_simpleTranslator.translate("jcr-tools.query.querySuccessMessage")).thenReturn("Query executed successfully");

        _querySubApp.doAction();

        verify(_view).buildResultTable(eq(queryResult), eq(true), eq(false), anyLong());
        verify(_subAppContext).openNotification(eq(MessageStyleTypeEnum.INFO), eq(true), eq("Query executed successfully"));
    }

    @Test
    void doActionStoresQueryInSession() throws RepositoryException {
        mockItemProperties(QUERY_LANGUAGE, WORKSPACE, STATEMENT, null, null);

        ContextMockUtils.mockQueryResult(WORKSPACE, QUERY_LANGUAGE, STATEMENT);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);

        _querySubApp.doAction();

        verify(mockWebContext()).setAttribute("QueryToolsLastQuery", STATEMENT, SESSION_SCOPE);
    }

    @Test
    void doActionHandlesRepositoryException() throws RepositoryException {
        mockItemProperties(QUERY_LANGUAGE, WORKSPACE, STATEMENT, "false", "true");

        when(_context.getJCRSession(WORKSPACE)).thenThrow(new RepositoryException("Test exception"));
        when(_simpleTranslator.translate("jcr-tools.query.queryFailedMessage")).thenReturn("Query execution failed");

        _querySubApp.doAction();

        verify(_view, never()).buildResultTable(any(), anyBoolean(), anyBoolean(), anyLong());
        verify(_subAppContext).openNotification(eq(MessageStyleTypeEnum.ERROR), eq(true), eq("Query execution failed"));
    }

    @Test
    void doActionWithShowScoreFalseAndShowColsTrue() throws RepositoryException {
        mockItemProperties(QUERY_LANGUAGE, WORKSPACE, STATEMENT, "false", "true");

        QueryResult queryResult = ContextMockUtils.mockQueryResult(WORKSPACE, QUERY_LANGUAGE, STATEMENT);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);
        when(_simpleTranslator.translate("jcr-tools.query.querySuccessMessage")).thenReturn("Query executed successfully");

        _querySubApp.doAction();

        verify(_view).buildResultTable(eq(queryResult), eq(false), eq(true), anyLong());
    }

    @Test
    void onSubAppStartRestoresLastQueryFromSession() throws Exception {
        String lastQuery = "SELECT * FROM [mgnl:page]";
        mockWebContext(stubAttribute("QueryToolsLastQuery", lastQuery, SESSION_SCOPE));

        FormDefinition formDefinition = mock(FormDefinition.class);
        TabDefinition tabDefinition = mock(TabDefinition.class);
        TextFieldDefinition statementFieldDefinition = new TextFieldDefinition();
        statementFieldDefinition.setName("statement");

        TextFieldDefinition workspaceFieldDefinition = new TextFieldDefinition();
        workspaceFieldDefinition.setName("workspace");

        List<FieldDefinition> fields = new ArrayList<>();
        fields.add(statementFieldDefinition);
        fields.add(workspaceFieldDefinition);

        when(tabDefinition.getFields()).thenReturn(fields);
        when(formDefinition.getTabs()).thenReturn(Collections.singletonList(tabDefinition));

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        when(_subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(formDefinition);

        QuerySubApp app = new QuerySubApp(_subAppContext, _formView, _view, _builder, _simpleTranslator, _contextProvider);
        app.onSubAppStart();
        assertEquals(lastQuery, statementFieldDefinition.getDefaultValue());
    }

    @Test
    void onSubAppStartWithoutLastQueryInSession() throws Exception {
        mockWebContext();

        FormDefinition formDefinition = mock(FormDefinition.class);
        TabDefinition tabDefinition = mock(TabDefinition.class);
        TextFieldDefinition statementFieldDefinition = new TextFieldDefinition();
        statementFieldDefinition.setName("statement");

        List<FieldDefinition> fields = Collections.singletonList(statementFieldDefinition);

        when(tabDefinition.getFields()).thenReturn(fields);
        when(formDefinition.getTabs()).thenReturn(Collections.singletonList(tabDefinition));

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        when(_subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(formDefinition);

        QuerySubApp querySubApp = new QuerySubApp(_subAppContext, _formView, _view, _builder, _simpleTranslator, _contextProvider);
        querySubApp.onSubAppStart();
        assertEquals(null, statementFieldDefinition.getDefaultValue());
    }

    @Test
    void onSubAppStartWithEmptyLastQueryInSession() throws Exception {
        MgnlContext.setAttribute("QueryToolsLastQuery", "", SESSION_SCOPE);

        FormDefinition formDefinition = mock(FormDefinition.class);
        TabDefinition tabDefinition = mock(TabDefinition.class);
        TextFieldDefinition statementFieldDefinition = new TextFieldDefinition();
        statementFieldDefinition.setName("statement");

        List<FieldDefinition> fields = Collections.singletonList(statementFieldDefinition);

        when(tabDefinition.getFields()).thenReturn(fields);
        when(formDefinition.getTabs()).thenReturn(Collections.singletonList(tabDefinition));

        FormSubAppDescriptor descriptor = mock(FormSubAppDescriptor.class);
        when(_subAppContext.getSubAppDescriptor()).thenReturn(descriptor);
        when(descriptor.getForm()).thenReturn(formDefinition);

        QuerySubApp querySubApp = new QuerySubApp(_subAppContext, _formView, _view, _builder, _simpleTranslator, _contextProvider);
        querySubApp.onSubAppStart();
        assertNull(statementFieldDefinition.getDefaultValue());
    }

    @Test
    void doActionWithNullShowScoreAndShowCols() throws RepositoryException {
        mockItemProperties(QUERY_LANGUAGE, WORKSPACE, STATEMENT, null, null);

        QueryResult queryResult = ContextMockUtils.mockQueryResult(WORKSPACE, QUERY_LANGUAGE, STATEMENT);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);
        when(_simpleTranslator.translate("jcr-tools.query.querySuccessMessage")).thenReturn("Query executed successfully");

        _querySubApp.doAction();

        verify(_view).buildResultTable(eq(queryResult), eq(false), eq(false), anyLong());
    }

    @Test
    void doActionWithDifferentQueryLanguage() throws RepositoryException {
        String xpathQuery = "//element(*, mgnl:page)";
        String xpathLanguage = "xpath";

        mockItemProperties(xpathLanguage, WORKSPACE, xpathQuery, "true", "true");

        QueryManager queryManager = ContextMockUtils.mockQueryManager(WORKSPACE);
        QueryResult queryResult = ContextMockUtils.mockQueryResult(WORKSPACE, xpathLanguage, xpathQuery);
        Session session = MgnlContext.getJCRSession(WORKSPACE);
        when(_context.getJCRSession(WORKSPACE)).thenReturn(session);

        when(_simpleTranslator.translate("jcr-tools.query.querySuccessMessage")).thenReturn("Query executed successfully");

        _querySubApp.doAction();

        verify(queryManager).createQuery(xpathQuery, xpathLanguage);
        verify(_view).buildResultTable(eq(queryResult), eq(true), eq(true), anyLong());
    }

    void mockItemProperties(final String queryLanguage, final String workspace, final String statement, final String showPaths, final String showCols) {
        Property queryLanguageProperty = mock(Property.class);
        Property workspaceProperty = mock(Property.class);
        Property statementProperty = mock(Property.class);
        Property showScoreProperty = mock(Property.class);
        Property showColsProperty = mock(Property.class);

        when(_item.getItemProperty("queryLanguage")).thenReturn(queryLanguageProperty);
        when(_item.getItemProperty("workspace")).thenReturn(workspaceProperty);
        when(_item.getItemProperty("statement")).thenReturn(statementProperty);
        when(_item.getItemProperty("showPaths")).thenReturn(showScoreProperty);
        when(_item.getItemProperty("showCols")).thenReturn(showColsProperty);

        when(queryLanguageProperty.getValue()).thenReturn(queryLanguage);
        when(workspaceProperty.getValue()).thenReturn(workspace);
        when(statementProperty.getValue()).thenReturn(statement);
        when(showScoreProperty.getValue()).thenReturn(showPaths);
        when(showColsProperty.getValue()).thenReturn(showCols);
    }
}

