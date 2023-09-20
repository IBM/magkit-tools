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
import com.vaadin.v7.ui.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * Query result tables.
 *
 * @author oliver.emke
 * @since 1.5.0
 */
public class QueryResultTable extends Table {
    private static final long serialVersionUID = 4438113723374596640L;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryResultTable.class);
    private static final float RESULT_TABLE_WIDTH_PERCENT = 100f;

    @Inject
    public QueryResultTable() {
        setWidth(RESULT_TABLE_WIDTH_PERCENT, PERCENTAGE);
        setStyleName("searchresult");
        setSortEnabled(true);
    }

    public void buildResultTable(final QueryResult queryResult, final boolean showScore, final boolean showCols, final long duration) {
        try {
            if (queryResult != null) {
                if (showScore) {
                    addContainerProperty("path", String.class, null);
                    addContainerProperty("score", String.class, null);
                }
                if (showCols) {
                    for (final String columnName : queryResult.getColumnNames()) {
                        addContainerProperty(normalizeColumnName(columnName), String.class, null);
                    }
                }

                int size = 0;
                RowIterator rows = queryResult.getRows();
                while (rows.hasNext()) {
                    size++;
                    addTableItem(rows.nextRow(), queryResult.getSelectorNames(), queryResult.getColumnNames(), showScore, showCols);
                }
                setCaption("Query result: " + size + " rows in " + duration + "ms");
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error message", e);
        }
    }

    protected String normalizeColumnName(final String columnName) {
        String normalizedColName = columnName;
        if (normalizedColName.contains(":") && normalizedColName.indexOf('.') > normalizedColName.indexOf(":")) {
            normalizedColName = substringAfter(normalizedColName, ".");
        }
        return normalizedColName;
    }

    private void addTableItem(final Row row, final String[] selectorNames, final String[] columnNames, final boolean showScore, final boolean showCols) throws RepositoryException {
        Item newItem = getItem(addItem());
        if (showScore) {
            StringBuilder paths = new StringBuilder();
            StringBuilder scores = new StringBuilder();
            for (String selectorName : selectorNames) {
                paths.append(retrievePrefix(selectorName)).append(row.getPath(selectorName));
                scores.append(retrievePrefix(selectorName)).append(row.getScore(selectorName));
            }
            newItem.getItemProperty("path").setValue(paths.toString());
            newItem.getItemProperty("score").setValue(scores.toString());
        }

        if (showCols) {
            for (String columnName : columnNames) {
                Value columnValue = row.getValue(columnName);
                if (columnValue != null) {
                    newItem.getItemProperty(normalizeColumnName(columnName)).setValue(columnValue.getString());
                }
            }
        }
    }

    private String retrievePrefix(String selectorName) {
        return selectorName.contains(":") ? "" : (selectorName + " - ");
    }
}
