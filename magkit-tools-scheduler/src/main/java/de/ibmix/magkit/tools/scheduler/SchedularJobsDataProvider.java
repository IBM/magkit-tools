package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Scheduler
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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
import de.ibmix.magkit.query.sql2.Sql2;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.ui.contentapp.JcrDataProvider;
import info.magnolia.ui.contentapp.JcrDataProviderUtils;
import info.magnolia.ui.contentapp.JcrQueryBuilder;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.filter.DataFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.stream.Stream;

import static info.magnolia.ui.contentapp.JcrDataProviderUtils.streamJcrItems;

/**
 * Data provider for scheduled job nodes backed by a Magnolia JCR datasource.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Retrieves child nodes under the configured root path</li>
 *   <li>Applies the first allowed node type from the datasource definition (if any)</li>
 *   <li>Builds a JCR-SQL2 query via the fluent {@code Sql2} helper</li>
 *   <li>Returns results as a {@link Stream} for Vaadin data binding</li>
 * </ul>
 * <p><strong>Usage Preconditions:</strong></p>
 * A valid {@link JcrDatasourceDefinition} with workspace and root path must be provided. Allowed node types list may
 * be empty; in that case all child nodes are returned.
 * <p><strong>Null &amp; Error Handling:</strong></p>
 * The implementation does not return {@code null}. If the definition contains no allowed node types an empty string is
 * used, delegating filtering to the underlying query builder. Repository access errors thrown by lower layers will
 * propagate as runtime exceptions handled by the UI framework.
 * <p><strong>Side Effects:</strong></p>
 * No modification of repository content; only read operations are performed.
 * <p><strong>Thread-Safety:</strong></p>
 * Instances are typically used in a single Vaadin UI thread. The internal state is immutable after construction and
 * thus safe for concurrent read access, but Vaadin data providers are not designed for multi-threaded UI interaction.
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * JcrDatasourceDefinition def = ...; // configured with workspace "jobs" and rootPath "/jobs"
 * JcrDatasource ds = ...;
 * SchedularJobsDataProvider provider = new SchedularJobsDataProvider(def, ds);
 * Stream<Node> nodes = provider.fetchFromBackEnd(new Query<>(0, 50, null, null, null));
 * nodes.forEach(node -> { // process node });
 * }</pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-28
 */
public class SchedularJobsDataProvider extends JcrDataProvider {

    private final JcrDatasourceDefinition _definition;
    private final JcrDatasource _datasource;

    /**
     * Constructs a new data provider for scheduled job nodes.
     * Retains a reference to the datasource definition for later query construction.
     *
     * @param definition the JCR datasource definition containing workspace, root path and optional node types
     * @param datasource the JCR datasource used for repository access
     */
    @Inject
    public SchedularJobsDataProvider(JcrDatasourceDefinition definition, JcrDatasource datasource) {
        super(definition, datasource);
        _definition = definition;
        _datasource = datasource;
    }

    /**
     * Returns the first allowed JCR node type or an empty string if no node types are configured.
     *
     * @return first allowed node type or empty string
     */
    protected String getFirstAllowedNodeType() {
        return _definition.getAllowedNodeTypes().isEmpty() ? StringUtils.EMPTY : _definition.getAllowedNodeTypes().iterator().next();
    }

    /**
     * Fetches nodes from the backend by executing a SQL2 query limited to children of the configured root path and
     * optionally filtered by the first allowed node type.
     * <p><strong>Important Details:</strong></p>
     * The provided {@link DataFilter} in the {@link Query} is currently ignored; additional filtering can be added
     * later by extending the SQL2 condition set.
     *
     * @param query the Vaadin data provider query (offset, limit, optional filter) – filter may be ignored
     * @return stream of matching JCR nodes (never {@code null})
     */
    @Override
    public Stream<Node> fetchFromBackEnd(Query<Node, DataFilter> query) {
        NodeIterator result = new NodeIteratorAdapter(Sql2.Query.nodesFrom(_definition.getWorkspace(), getFirstAllowedNodeType(),
            Sql2.Condition.Path.isChild(_definition.getRootPath())
        ));
        // Add result handling from JcrDataProvider
        boolean queryFilteringStatus = JcrQueryBuilder.isFilteringStatus(query);
        if (queryFilteringStatus) {
            result = new FilteringNodeIterator(result, new JcrDataProviderUtils.ActivationStatusFilteringPredicate(query));
        }
        Stream<Node> stream = streamJcrItems(result).map(item -> _datasource.wrapItem((Item) item));
        if (queryFilteringStatus) {
            stream = stream
                // We don't use javax.jcr.query.Query.setLimit because the result is post processed by
                // FilteringNodeIterator.
                // This should not be a performance issue since the same call (with size max) is done by info
                // .magnolia.ui.contentapp.JcrDataProvider#sizeInBackEnd.
                // As the query result is already cached by jackrabbit, this might be even better performance wise.
                .skip(query.getOffset())
                .limit(query.getLimit());
        }
        return stream;
    }
}
