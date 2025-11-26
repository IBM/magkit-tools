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

import com.vaadin.server.Sizeable;
import com.vaadin.v7.data.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import java.util.Iterator;

import static de.ibmix.magkit.test.jcr.RowStubbingOperation.stubPath;
import static de.ibmix.magkit.test.jcr.RowStubbingOperation.stubValue;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockRow;
import static de.ibmix.magkit.test.jcr.query.QueryMockUtils.mockRowQueryResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for QueryResultTable column name normalization.
 *
 * @author frank.sommer@ibmix.de
 * @since 2017-12-05
 */
public class QueryResultTableTest {

    private QueryResultTable _resultTable;

    @BeforeEach
    void setUp() {
        _resultTable = new QueryResultTable();
    }

    @Test
    void testConstructor() {
        QueryResultTable table = new QueryResultTable();

        assertNotNull(table);
        assertEquals(100f, table.getWidth());
        assertEquals(Sizeable.Unit.PERCENTAGE, table.getWidthUnits());
        assertEquals("searchresult", table.getStyleName());
        assertTrue(table.isSortEnabled());
    }

    @ParameterizedTest
    @CsvSource({
        "test, test",
        "test.jcr:type, test.jcr:type",
        "mgnl:page.test, test",
        "mgnl:page.jcr:type, jcr:type",
        "jcr:type, jcr:type"
    })
    void testNormalizeColumnName(String input, String expected) {
        assertEquals(expected, _resultTable.normalizeColumnName(input));
    }

    @ParameterizedTest
    @CsvSource({
        "jcr:content, ''",
        "page, 'page - '",
        "'', ' - '"
    })
    void testRetrievePrefix(String input, String expected) {
        assertEquals(expected, _resultTable.retrievePrefix(input));
    }

    @Test
    void testBuildResultTableWithNullQueryResult() {
        _resultTable.buildResultTable(null, true, true, 100L);

        assertEquals(0, _resultTable.size());
        assertNull(_resultTable.getCaption());
    }

    @Test
    void testBuildResultTableWithShowScoreAndCols() throws RepositoryException {
        QueryResult queryResult = mockRowQueryResult(
            mockRow(0.5, stubPath("mgnl:page", "/test/path"), stubValue("jcr:title", "Test Title"), stubValue("jcr:uuid", "test-uuid"))
        );

        String[] selectorNames = new String[]{"mgnl:page"};
        when(queryResult.getSelectorNames()).thenReturn(selectorNames);
        String[] columnNames = new String[]{"jcr:title", "jcr:uuid"};
        when(queryResult.getColumnNames()).thenReturn(columnNames);

        _resultTable.buildResultTable(queryResult, true, true, 150L);

        assertEquals(1, _resultTable.size());
        assertEquals("Query result: 1 rows in 150ms", _resultTable.getCaption());
        assertNotNull(_resultTable.getContainerProperty(_resultTable.getItemIds().iterator().next(), "path"));
        assertNotNull(_resultTable.getContainerProperty(_resultTable.getItemIds().iterator().next(), "score"));
        assertNotNull(_resultTable.getContainerProperty(_resultTable.getItemIds().iterator().next(), "jcr:title"));
        assertNotNull(_resultTable.getContainerProperty(_resultTable.getItemIds().iterator().next(), "jcr:uuid"));
    }

    @Test
    void testBuildResultTableWithMultipleRows() throws RepositoryException {
        QueryResult queryResult = mockRowQueryResult(
            mockRow(0.8, stubPath("mgnl:page", "/path1"), stubValue("jcr:title", "Test Title")),
            mockRow(0.6, stubPath("mgnl:page", "/path2"), stubValue("jcr:title", "Test Title 2"))
        );

        String[] selectorNames = new String[]{"mgnl:page"};
        when(queryResult.getSelectorNames()).thenReturn(selectorNames);
        String[] columnNames = new String[]{"title"};
        when(queryResult.getColumnNames()).thenReturn(columnNames);

        _resultTable.buildResultTable(queryResult, true, false, 200L);

        assertEquals(2, _resultTable.size());
        assertEquals("Query result: 2 rows in 200ms", _resultTable.getCaption());
    }

    @Test
    void testBuildResultTableWithoutShowScore() throws RepositoryException {
        QueryResult queryResult = mockRowQueryResult(
            mockRow(0.5, stubPath("mgnl:page", "/test/path"), stubValue("jcr:title", "Test Title"))
        );

        String[] selectorNames = new String[]{"mgnl:page"};
        when(queryResult.getSelectorNames()).thenReturn(selectorNames);
        String[] columnNames = new String[]{"title"};
        when(queryResult.getColumnNames()).thenReturn(columnNames);

        _resultTable.buildResultTable(queryResult, false, true, 100L);

        assertEquals(1, _resultTable.size());
        Object itemId = _resultTable.getItemIds().iterator().next();
        assertNull(_resultTable.getContainerProperty(itemId, "path"));
        assertNotNull(_resultTable.getContainerProperty(itemId, "title"));
    }

    @Test
    void testBuildResultTableWithoutShowCols() throws RepositoryException {
        QueryResult queryResult = mockRowQueryResult(
            mockRow(1.0, stubPath("mgnl:page", "/test"), stubValue("jcr:title", "Title"))
        );

        String[] selectorNames = new String[]{"mgnl:page"};
        when(queryResult.getSelectorNames()).thenReturn(selectorNames);
        String[] columnNames = new String[]{"title"};
        when(queryResult.getColumnNames()).thenReturn(columnNames);

        _resultTable.buildResultTable(queryResult, true, false, 50L);

        assertEquals(1, _resultTable.size());
        Object itemId = _resultTable.getItemIds().iterator().next();
        assertNotNull(_resultTable.getContainerProperty(itemId, "path"));
        assertNotNull(_resultTable.getContainerProperty(itemId, "score"));
        assertNull(_resultTable.getContainerProperty(itemId, "title"));
    }

    @Test
    void testBuildResultTableWithMultipleSelectors() throws RepositoryException {
        Row row1 = mockRow(0.9, stubPath("page", "/page/path"));
        doReturn(0.9).when(row1).getScore("page");
        Row row2 = mockRow(0.7, stubPath("asset", "/asset/path"));
        doReturn(0.7).when(row2).getScore("asset");
        QueryResult queryResult = mockRowQueryResult(
            row1, row2
        );

        String[] selectorNames = new String[]{"page", "asset"};
        when(queryResult.getSelectorNames()).thenReturn(selectorNames);
        String[] columnNames = new String[]{};
        when(queryResult.getColumnNames()).thenReturn(columnNames);

        _resultTable.buildResultTable(queryResult, true, false, 300L);

        assertEquals(2, _resultTable.size());
        Iterator<?> itemIds = _resultTable.getItemIds().iterator();
        Item item = _resultTable.getItem(itemIds.next());
        String path = (String) item.getItemProperty("path").getValue();
        String score = (String) item.getItemProperty("score").getValue();
        // this is strange, but according to the implementation, if there is no value, "null" is appended
        assertEquals("page - /page/path", path);
        assertEquals("page - 0.9", score);

        item = _resultTable.getItem(itemIds.next());
        path = (String) item.getItemProperty("path").getValue();
        score = (String) item.getItemProperty("score").getValue();
        // this is strange, but according to the implementation, if there is no value, "null" is appended
        assertEquals("asset - /asset/path", path);
        assertEquals("asset - 0.7", score);
    }

    @Test
    void testAddTableItemWithNullValue() throws RepositoryException {
        QueryResult queryResult = mockRowQueryResult(
            mockRow(1.0, stubPath("page", "/test"))
        );

        String[] selectorNames = new String[]{"page"};
        when(queryResult.getSelectorNames()).thenReturn(selectorNames);
        String[] columnNames = new String[]{"title", "description"};
        when(queryResult.getColumnNames()).thenReturn(columnNames);

        _resultTable.buildResultTable(queryResult, false, true, 100L);

        assertEquals(1, _resultTable.size());
        Object itemId = _resultTable.getItemIds().iterator().next();
        assertNotNull(_resultTable.getContainerProperty(itemId, "title"));
        assertNull(_resultTable.getContainerProperty(itemId, "title").getValue());
        assertNotNull(_resultTable.getContainerProperty(itemId, "description"));
        assertNull(_resultTable.getContainerProperty(itemId, "description").getValue());
    }

    @Test
    void testBuildResultTableWithRepositoryException() throws RepositoryException {
        QueryResult queryResult = mock(QueryResult.class);

        when(queryResult.getColumnNames()).thenThrow(new RepositoryException("Test exception"));

        _resultTable.buildResultTable(queryResult, true, true, 100L);

        assertEquals(0, _resultTable.size());
    }
}
