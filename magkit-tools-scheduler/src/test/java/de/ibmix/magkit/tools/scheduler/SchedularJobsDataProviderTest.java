package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * magkit-tools-scheduler
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

import de.ibmix.magkit.query.sql2.Sql2;
import de.ibmix.magkit.query.sql2.condition.Sql2JoinConstraint;
import de.ibmix.magkit.test.jcr.query.QueryStubbingOperation;
import info.magnolia.context.Context;
import info.magnolia.periscope.rank.ResultRankerFactory;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.filter.DataFilter;
import info.magnolia.ui.filter.FilterOperator;
import info.magnolia.ui.filter.FilterValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <p><strong>Tests:</strong></p>
 * Verifies constructor wiring, allowed node type selection, property condition mapping, operand/operator conversion,
 * and origin value extraction.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-28
 */
class SchedularJobsDataProviderTest {

    private JcrDatasourceDefinition _definition;
    private JcrDatasource _datasource;

    @BeforeEach
    void setUp() {
        mockComponentInstance(Context.class);
        mockComponentInstance(ResultRankerFactory.class);
        _definition = mock(JcrDatasourceDefinition.class);
        _datasource = mock(JcrDatasource.class);
        when(_definition.getWorkspace()).thenReturn("config");
        when(_definition.getRootPath()).thenReturn("/jobs");
    }

    @Test
    void constructorInitializesDefinition() {
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        assertNotNull(provider);
    }

    @Test
    void getFirstAllowedNodeTypeReturnsEmptyWhenNoneConfigured() {
        when(_definition.getAllowedNodeTypes()).thenReturn(Collections.emptySet());
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        String nodeType = provider.getFirstAllowedNodeType();
        assertEquals("", nodeType);
    }

    @Test
    void getFirstAllowedNodeTypeReturnsFirstConfigured() {
        Set<String> nodeTypes = new LinkedHashSet<>(Arrays.asList("mgnl:job", "mgnl:other"));
        when(_definition.getAllowedNodeTypes()).thenReturn(nodeTypes);
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        String nodeType = provider.getFirstAllowedNodeType();
        assertEquals("mgnl:job", nodeType);
    }

    @Test
    void getPropertyConditionsHandlesNodeNameSpecialCaseAndIgnoresPublishingStatus() {
        Map<String, FilterValue<String>> filters = new LinkedHashMap<>();
        FilterValue<String> nodeNameValue = new FilterValue<>(FilterOperator.EQUALS, "JobA");
        FilterValue<String> ignoredValue = new FilterValue<>(FilterOperator.CONTAINS, "anything");
        filters.put("jcr:nodeName", nodeNameValue);
        filters.put("jcrPublishingStatus", ignoredValue);
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        List<?> conditions = provider.getPropertyConditions(filters);
        assertEquals(1, conditions.size());
    }

    @Test
    void getPropertyConditionsSkipsEmptyOriginValues() {
        Map<String, FilterValue<String>> filters = new LinkedHashMap<>();
        FilterValue<String> emptyValue = new FilterValue<>(FilterOperator.EQUALS, "");
        filters.put("title", emptyValue);
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        List<?> conditions = provider.getPropertyConditions(filters);
        assertEquals(0, conditions.size());
    }

    @Test
    void toSql2JoinConstraintReturnsNullForUnsupportedTypes() {
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        Integer[] values = new Integer[]{1, 2};
        assertNull(provider.toSql2JoinConstraint("prop", FilterOperator.EQUALS, values));
    }

    @Test
    void toSql2JoinConstraintReturnsConstraintForStringArray() {
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        String[] values = new String[]{"a", "b"};
        Sql2JoinConstraint constraint = provider.toSql2JoinConstraint("title", FilterOperator.CONTAINS, values);
        assertNotNull(constraint);
        assertEquals("([title] LIKE '%a%' OR [title] LIKE '%b%')", constraint.asString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void toSql2StaticOperandRespectsOperators() {
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        de.ibmix.magkit.query.sql2.condition.Sql2DynamicOperand operand = Mockito.mock(de.ibmix.magkit.query.sql2.condition.Sql2DynamicOperand.class);
        de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple<String> likeResult = Mockito.mock(de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple.class);
        de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple<String> startsWithResult = Mockito.mock(de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple.class);
        de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple<String> equalsResult = Mockito.mock(de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple.class);
        when(operand.likeAny()).thenReturn(likeResult);
        when(operand.startsWithAny()).thenReturn(startsWithResult);
        when(operand.equalsAny()).thenReturn(equalsResult);
        de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple<String> containsMapped = provider.toSql2StaticOperand(operand, FilterOperator.CONTAINS);
        de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple<String> startsMapped = provider.toSql2StaticOperand(operand, FilterOperator.STARTS_WITH);
        de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple<String> equalsMapped = provider.toSql2StaticOperand(operand, FilterOperator.EQUALS);
        assertEquals(likeResult, containsMapped);
        assertEquals(startsWithResult, startsMapped);
        assertEquals(equalsResult, equalsMapped);
        verify(operand).likeAny();
        verify(operand).startsWithAny();
        verify(operand).equalsAny();
    }

    @Test
    <T> void getOriginValuesReturnsArrayForIterableAndSingleValue() {
        DataFilter iterableValue = new DataFilter();
        iterableValue.filter("iterable", FilterOperator.CONTAINS, Arrays.asList("x", "y"));
        DataFilter singleValue = new DataFilter();
        singleValue.filter("single", FilterOperator.STARTS_WITH, "z");
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        T[] iterableArray = provider.getOriginValues(iterableValue.getPropertyFilters().get("iterable"));
        T[] singleArray = provider.getOriginValues(singleValue.getPropertyFilters().get("single"));
        List<T> iterableList = Arrays.asList(iterableArray);
        assertEquals(Arrays.asList("x", "y"), iterableList);
        assertEquals(1, singleArray.length);
        assertEquals("z", singleArray[0]);
    }

    @Test
    void getNodeIteratorBuildsExpectedSql2AndExecutesQuery() throws RepositoryException {
        Set<String> nodeTypes = Set.of("mgnl:job");
        when(_definition.getAllowedNodeTypes()).thenReturn(nodeTypes);
        when(_definition.getWorkspace()).thenReturn("config");
        when(_definition.getRootPath()).thenReturn("/jobs");
        QueryManager queryManager = mockQueryManager("config");

        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        assertThrows(IllegalArgumentException.class, () -> provider.getNodeIterator(null));

        String expectedStatement = Sql2.Statement.select()
            .from("mgnl:job").as("n").whereAll(
                Sql2.Condition.Path.isChild("/jobs")
            ).toString();
        Query queryMock = mockQuery("config", Query.JCR_SQL2, expectedStatement, QueryStubbingOperation.stubResult());
        provider.getNodeIterator(new com.vaadin.data.provider.Query<>(0, 10, null, null, null));
        verify(queryManager).createQuery(expectedStatement, Query.JCR_SQL2);
        verify(queryMock).execute();

        DataFilter filter = new DataFilter();
        filter.setFullTextSearchStatement("foo");
        expectedStatement = Sql2.Statement.select()
            .from("mgnl:job").as("n").whereAll(
                Sql2.Condition.Path.isChild("/jobs"),
                Sql2.Condition.FullText.contains().any("foo*")
            ).toString();
        queryMock = mockQuery("config", Query.JCR_SQL2, expectedStatement, QueryStubbingOperation.stubResult());
        provider.getNodeIterator(new com.vaadin.data.provider.Query<>(0, 10, null, null, filter));
        verify(queryManager).createQuery(expectedStatement, Query.JCR_SQL2);
        verify(queryMock).execute();
    }

    @AfterEach
    void tearDown() {
        cleanContext();
    }
}
