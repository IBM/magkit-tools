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
import info.magnolia.context.Context;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.periscope.rank.ResultRankerFactory;
import info.magnolia.ui.contentapp.JcrDataProvider;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrDatasourceDefinition;
import info.magnolia.ui.filter.DataFilter;
import jakarta.inject.Inject;
import lombok.NonNull;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * Data provider for scheduled job nodes backed by a Magnolia JCR datasource.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-28
 */
public class SchedularJobsDataProvider extends JcrDataProvider {
    private static final int DEPTH_JOBS = 5;

    /**
     * Constructs a new data provider for scheduled job nodes.
     *
     * @param definition          the JCR datasource definition containing workspace, root path, and optional node types
     * @param datasource          the JCR datasource used for repository access
     * @param resultRankerFactory the factory for creating result rankers
     * @param context             the magnolia context
     */
    @Inject
    public SchedularJobsDataProvider(JcrDatasourceDefinition definition, JcrDatasource datasource, ResultRankerFactory resultRankerFactory, Context context) {
        super(definition, datasource, resultRankerFactory, context);
    }

    /**
     * Retrieves a filtered {@link NodeIterator} from the given {@link Query}.
     * Filters nodes to include only those at the scheduler jobs depth, as defined by the {@code DEPTH_JOBS} field.
     *
     * @param query the {@link Query} object used to fetch nodes from the repository
     * @return a {@link NodeIterator} containing only nodes that match the specified filter criteria
     * @throws RepositoryException if an error occurs while accessing the repository
     */
    @Override
    protected NodeIterator getNodeIterator(@NonNull Query<Node, DataFilter> query) throws RepositoryException {
        return new FilteringNodeIterator(super.getNodeIterator(query), new AbstractPredicate<>() {
            @Override
            public boolean evaluateTyped(Node node) {
                try {
                    return node.getDepth() == DEPTH_JOBS;
                } catch (RepositoryException e) {
                    return false;
                }
            }
        });
    }
}
