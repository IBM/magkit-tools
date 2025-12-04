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

import com.machinezoo.noexception.Exceptions;
import com.vaadin.data.provider.Query;
import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.query.sql2.Sql2;
import de.ibmix.magkit.query.sql2.condition.Sql2DynamicOperand;
import de.ibmix.magkit.query.sql2.condition.Sql2JoinConstraint;
import de.ibmix.magkit.query.sql2.condition.Sql2StaticOperandMultiple;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.ui.contentapp.JcrDataProvider;
import info.magnolia.ui.contentapp.JcrDataProviderUtils;
import info.magnolia.ui.contentapp.JcrQueryBuilder;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.filter.DataFilter;
import info.magnolia.ui.filter.FilterOperator;
import info.magnolia.ui.filter.FilterValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
     * <p><strong>Description:</strong></p>
     * Initializes the provider by delegating to the parent {@link JcrDataProvider} and retaining references
     * to the given definition and datasource for later query construction.
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
     * <p><strong>Key Details:</strong></p>
     * Uses the order from {@link JcrDatasourceDefinition#getAllowedNodeTypes()} and falls back to
     * {@link StringUtils#EMPTY} if the set is empty.
     *
     * @return first allowed node type or empty string
     */
    protected String getFirstAllowedNodeType() {
        return _definition.getAllowedNodeTypes().isEmpty() ? StringUtils.EMPTY : _definition.getAllowedNodeTypes().iterator().next();
    }

    /**
     * Builds and executes a JCR-SQL2 query to fetch nodes according to the provided {@link DataFilter} and pagination.
     * <p><strong>Main Functionalities:</strong></p>
     * Applies property filters, full-text search, and constrains results to children of the configured root path.
     * The query targets the first allowed node type if present.
     *
     * @param query the Vaadin query containing offset, limit, and optional {@link DataFilter}
     * @return an iterator over matching nodes
     * @throws RepositoryException if repository access fails during query execution
     */
    @Override
    protected NodeIterator getNodeIterator(Query<Node, DataFilter> query) throws RepositoryException {
        Require.Argument.notNull(query, "Query must not be null.");
        Map<String, Object> filters = query.getFilter().map(DataFilter::getPropertyFilters).orElse(Collections.emptyMap());
        String fullTextSearch = query.getFilter().map(DataFilter::getFullTextSearchStatement).orElse(StringUtils.EMPTY);
        List<Sql2JoinConstraint> conditions = getPropertyConditions(filters);
        conditions.add(Sql2.Condition.Path.isChild(_definition.getRootPath()));
        if (isNotEmpty(fullTextSearch)) {
            conditions.add(Sql2.Condition.FullText.contains().any(fullTextSearch + '*'));
        }

        NodeIterator nodeIterator = new NodeIteratorAdapter(Sql2.Query.nodesFrom(_definition.getWorkspace()).withStatement(
            Sql2.Statement.select().from(getFirstAllowedNodeType()).as("n").whereAll(
                conditions.toArray(new Sql2JoinConstraint[0])
            )
        ).getResultNodes());

        if (JcrQueryBuilder.isFilteringStatus(query)) {
            nodeIterator = new FilteringNodeIterator(nodeIterator, new JcrDataProviderUtils.ActivationStatusFilteringPredicate(query));
        }
        return nodeIterator;
    }

    /**
     * Translates UI property filters into {@link Sql2JoinConstraint} conditions used in the JCR-SQL2 query.
     * <p><strong>Key Features:</strong></p>
     * Special-cases {@code jcr:nodeName} for case-insensitive equality and ignores {@code jcrPublishingStatus}.
     * Other string properties are mapped according to the provided {@link FilterOperator}.
     *
     * @param filters a map of property names to filter values coming from the {@link DataFilter}
     * @param <T> the raw type of origin filter values
     * @return a list of join constraints to be applied in the query
     */
    <T> List<Sql2JoinConstraint> getPropertyConditions(Map<String, ? super FilterValue<T>> filters) {
        List<Sql2JoinConstraint> conditions = new ArrayList<>();
        final String nodeNameProp = "jcr:nodeName";
        filters.entrySet().stream()
            .filter(filterEntry -> filterEntry.getValue() != null
                && !StringUtils.EMPTY.equals(DataFilter.getFilterOriginValue(filterEntry.getValue())))
            .forEach(Exceptions.wrap().consumer(filterEntry -> {
                String filterEntryKey = filterEntry.getKey();
                Object filterEntryValue = filterEntry.getValue();
                FilterOperator filterOperator = DataFilter.getFilterOperator(filterEntryValue);
                T[] originValues = getOriginValues(filterEntryValue);
                Objects.requireNonNull(filterEntryKey, "Filter property must not be null.");
                if (filterEntryKey.equals(nodeNameProp)) {
                    conditions.add(Sql2.Condition.String.property(nodeNameProp).equalsAll().values(originValues[0].toString().toLowerCase()));
                } else if (!"jcrPublishingStatus".equals(filterEntryKey)) {
                    conditions.add(toSql2JoinConstraint(filterEntryKey, filterOperator, originValues));
                }
            })
        );
        return conditions;
    }

    /**
     * Converts a property name, operator and values into a single {@link Sql2JoinConstraint}.
     * <p><strong>Important Details:</strong></p>
     * Currently supports string values; returns {@code null} for unsupported types.
     *
     * @param propertyName the name of the property to filter
     * @param filterOperator the operator selected in the UI (e.g. CONTAINS, STARTS_WITH, EQUALS)
     * @param values the origin values to match against
     * @param <T> the generic value type
     * @return a join constraint or {@code null} if the type is not supported
     */
    <T> Sql2JoinConstraint toSql2JoinConstraint(String propertyName, FilterOperator filterOperator, T[] values) {
        if (values instanceof String[]) {
            return toSql2StaticOperand(Sql2.Condition.String.property(propertyName), filterOperator).values((String[]) values);
        } else {
            // other types can be added as needed
            return null;
        }
    }

    /**
     * Maps a dynamic operand and a filter operator to a multi-value static operand builder.
     * <p><strong>Main Functionalities:</strong></p>
     * Provides LIKE, STARTS WITH, or EQUALS matching depending on the operator.
     *
     * @param operand the dynamic operand representing the property in the query
     * @param filterOperator the operator defining how values should be matched
     * @return a static operand builder configured for multiple values
     */
    Sql2StaticOperandMultiple toSql2StaticOperand(Sql2DynamicOperand operand, FilterOperator filterOperator) {
        Sql2StaticOperandMultiple result = null;
        switch (filterOperator) {
            case CONTAINS:
                result = operand.likeAny();
                break;
            case STARTS_WITH:
                result = operand.startsWithAny();
                break;
            default:
                result = operand.equalsAny();
        }
        return result;
    }

    /**
     * Extracts origin values from a filter entry and returns them as a strongly typed array.
     * <p><strong>Key Features:</strong></p>
     * Supports single values and iterable collections; preserves element type for array creation.
     *
     * @param filterEntryValue the filter entry value container
     * @param <T> the element type of the origin values
     * @return an array containing the origin values
     */
    <T> T[] getOriginValues(Object filterEntryValue) {
        T values = DataFilter.getFilterOriginValue(filterEntryValue);
        if (values instanceof Iterable<?>) {
            List<T> valueList = new ArrayList<>();
            for (T value : (Iterable<T>) values) {
                valueList.add(value);
            }
            return valueList.toArray((T[]) java.lang.reflect.Array.newInstance(
                valueList.isEmpty() ? Object.class : valueList.get(0).getClass(), valueList.size()));
        } else {
            T singleValue = (T) values;
            T[] array = (T[]) java.lang.reflect.Array.newInstance(singleValue.getClass(), 1);
            array[0] = singleValue;
            return array;
        }
    }
}
