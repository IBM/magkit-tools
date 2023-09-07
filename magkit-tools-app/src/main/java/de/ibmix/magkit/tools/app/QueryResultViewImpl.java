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

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.CssLayout;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;

import javax.inject.Inject;
import javax.jcr.query.QueryResult;

/**
 * Query result view implementation.
 *
 * @author frank.sommer
 * @see QueryResultView
 * @since 1.5.0
 */
public class QueryResultViewImpl extends BaseResultViewImpl implements QueryResultView {

    @Inject
    public QueryResultViewImpl(final SubAppContext subAppContext, final ComponentProvider componentProvider, final FormBuilder formBuilder, final SimpleTranslator i18n) {
        super(subAppContext, componentProvider, formBuilder, i18n);

        CssLayout resultSection = getResultSection();
        BrowserFrame browserFrame = new BrowserFrame("Tipps zu JCR-SQL2", new ExternalResource(MgnlContext.getContextPath() + "/.resources/extended-tools/webresources/JcrQueryHelp.html"));
        browserFrame.setWidth("100%");
        browserFrame.setHeight("640px");
        browserFrame.addStyleName("help-frame");
        resultSection.addComponent(browserFrame);
    }

    @Override
    protected String getButtonKey() {
        return "queryTools.button.label";
    }

    public void buildResultTable(QueryResult queryResult, boolean showScore, boolean showCols, long duration) {
        QueryResultTable resultTable = new QueryResultTable();
        resultTable.buildResultTable(queryResult, showScore, showCols, duration);
        CssLayout resultSection = getResultSection();
        if (resultSection.getComponentCount() > 1) {
            resultSection.replaceComponent(resultSection.getComponent(0), resultTable);
        } else {
            resultSection.addComponentAsFirst(resultTable);
        }
    }
}
