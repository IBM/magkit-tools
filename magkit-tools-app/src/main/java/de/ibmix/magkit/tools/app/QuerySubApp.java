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
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.List;

import static info.magnolia.context.Context.SESSION_SCOPE;
import static org.apache.commons.lang.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * QuerySubApp prints the results of a JCR query.
 *
 * @author frank.sommer
 * @since 1.5.0
 */
public class QuerySubApp extends ToolsBaseSubApp<QueryResultView> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuerySubApp.class);
    private static final String PN_STATEMENT = "statement";
    private static final String PN_WORKSPACE = "workspace";

    private final QueryResultView _view;
    private final SimpleTranslator _simpleTranslator;
    private final Provider<Context> _contextProvider;
    private final FormViewReduced _formView;

    @Inject
    public QuerySubApp(final SubAppContext subAppContext, final FormViewReduced formView, final QueryResultView view, final FormBuilder builder, final SimpleTranslator simpleTranslator, final Provider<Context> contextProvider) {
        super(subAppContext, formView, view, builder);
        _formView = formView;
        _view = view;
        _simpleTranslator = simpleTranslator;
        _contextProvider = contextProvider;
    }

    @Override
    public void doAction() {
        final Item item = _formView.getItemDataSource();
        String queryLanguage = item.getItemProperty("queryLanguage").getValue().toString();
        String workspace = item.getItemProperty(PN_WORKSPACE).getValue().toString();
        String statement = item.getItemProperty(PN_STATEMENT).getValue().toString();
        Object showScoreValue = item.getItemProperty("showPaths").getValue();
        boolean showScore = showScoreValue != null && toBoolean(showScoreValue.toString());
        Object showColsValue = item.getItemProperty("showCols").getValue();
        boolean showCols = showColsValue != null && toBoolean(showColsValue.toString());

        MgnlContext.setAttribute("QueryToolsLastQuery", statement, SESSION_SCOPE);

        doQuery(workspace, statement, queryLanguage, showScore, showCols);
    }

    private void doQuery(final String workspace, final String statement, final String queryLanguage, final boolean showScore, final boolean showCols) {
        final long start = System.currentTimeMillis();

        try {
            final Session session = _contextProvider.get().getJCRSession(workspace);
            final QueryManager manager = session.getWorkspace().getQueryManager();
            final Query query = manager.createQuery(statement, queryLanguage);
            final QueryResult result = query.execute();
            long duration = System.currentTimeMillis() - start;
            _view.buildResultTable(result, showScore, showCols, duration);

            getSubAppContext().openNotification(MessageStyleTypeEnum.INFO, true, _simpleTranslator.translate("jcr-tools.query.querySuccessMessage"));
        } catch (RepositoryException e) {
            LOGGER.error(e.getMessage(), e);
            getSubAppContext().openNotification(MessageStyleTypeEnum.ERROR, true, _simpleTranslator.translate("jcr-tools.query.queryFailedMessage"));
        }
    }

    @Override
    protected void onSubAppStart() {
        String queryToolsLastQuery = MgnlContext.getAttribute("QueryToolsLastQuery", SESSION_SCOPE);
        if (isNotEmpty(queryToolsLastQuery)) {
            FormDefinition formDefinition = getFormDefinition();
            List<FieldDefinition> fields = formDefinition.getTabs().get(0).getFields();
            for (FieldDefinition field : fields) {
                if (field.getName().equals(PN_STATEMENT)) {
                    ((TextFieldDefinition) field).setDefaultValue(queryToolsLastQuery);
                }
            }
        }
        super.onSubAppStart();
    }
}
