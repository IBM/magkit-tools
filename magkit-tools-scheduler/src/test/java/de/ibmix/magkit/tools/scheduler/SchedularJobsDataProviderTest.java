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

import com.vaadin.data.provider.Query;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.jcr.query.QueryStubbingOperation;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SchedularJobsDataProvider}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-28
 */
class SchedularJobsDataProviderTest {

    private JcrDatasourceDefinition _definition;
    private JcrDatasource _datasource;

    @BeforeEach
    void setUp() {
        ContextMockUtils.cleanContext();
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
    void fetchFromBackEndBuildsQueryWithoutNodeType() throws RepositoryException {
        String expectedStatement = "SELECT * FROM [nt:base] WHERE ischildnode('/jobs')";
        javax.jcr.query.Query query = ContextMockUtils.mockQuery("config", javax.jcr.query.Query.JCR_SQL2, expectedStatement, QueryStubbingOperation.stubResult());
        when(_definition.getAllowedNodeTypes()).thenReturn(Collections.emptySet());
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        provider.fetchFromBackEnd(new Query<>(0, 10, null, null, null));
        verify(query).execute();
        verify(ContextMockUtils.mockQueryManager("config")).createQuery(expectedStatement, javax.jcr.query.Query.JCR_SQL2);
    }

    @Test
    void fetchFromBackEndBuildsQueryWithNodeType() throws RepositoryException {
        String expectedStatement = "SELECT * FROM [mgnl:job] WHERE ischildnode('/jobs')";
        javax.jcr.query.Query query = ContextMockUtils.mockQuery("config", javax.jcr.query.Query.JCR_SQL2, expectedStatement, QueryStubbingOperation.stubResult());
        when(_definition.getAllowedNodeTypes()).thenReturn(new LinkedHashSet<>(Collections.singletonList("mgnl:job")));
        SchedularJobsDataProvider provider = new SchedularJobsDataProvider(_definition, _datasource);
        provider.fetchFromBackEnd(new Query<>(0, 5, null, null, null));
        verify(query).execute();
        verify(ContextMockUtils.mockQueryManager("config")).createQuery(expectedStatement, javax.jcr.query.Query.JCR_SQL2);
    }
}
