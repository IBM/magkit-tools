package de.ibmix.magkit.tools.edit.ui;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
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

import de.ibmix.magkit.tools.edit.setup.EditToolsModule;
import de.ibmix.magkit.tools.edit.setup.StatusBarConfig;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.contentapp.DefaultItemDescriber;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static javax.jcr.query.Query.JCR_SQL2;

/**
 * Item describer that enhances the status bar display with asset usage information.
 * This component extends {@link DefaultItemDescriber} to append the number of references to an asset
 * across configured JCR workspaces.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Displays reference count for assets in the AdminCentral status bar</li>
 * <li>Searches for references across multiple configurable workspaces</li>
 * <li>Uses JCR SQL2 queries to find nodes referencing the asset's UUID</li>
 * <li>Only active when single item is selected</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong></p>
 * Configure the workspaces to search in the {@link StatusBarConfig} using the "assetUsageWorkspaces" property.
 *
 * <p><strong>Display Format:</strong></p>
 * Appends the reference count in parentheses to the item description (e.g., "/path/to/asset (5)").
 *
 * <p><strong>Performance:</strong></p>
 * The reference search is performed synchronously when displaying the status bar, which may impact
 * performance for assets with many references or when searching across multiple large workspaces.
 *
 * @author Philipp Güttler (IBM iX)
 * @since 2021-03-11
 */
public class AssetUsageItemDescriber extends DefaultItemDescriber<JcrNodeWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetUsageItemDescriber.class);

    private final Provider<EditToolsModule> _editToolsModule;

    @Inject
    public AssetUsageItemDescriber(Provider<EditToolsModule> editToolsModule) {
        _editToolsModule = editToolsModule;
    }

    /**
     * Applies the item description logic. For single item selections, appends usage information.
     *
     * @param items the collection of selected items
     * @return the item description with optional usage count
     */
    @Override
    public String apply(final Collection<JcrNodeWrapper> items) {
        return items.size() == 1 ? applySingle(items.iterator().next()) : super.apply(items);
    }

    /**
     * Creates the description for a single item, including the reference count across configured workspaces.
     *
     * @param item the selected JCR node
     * @return the item path with reference count appended in parentheses, or just the path if no configuration exists
     */
    public String applySingle(final JcrNodeWrapper item) {
        return NodeUtil.getPathIfPossible(item) + Optional.ofNullable(_editToolsModule.get().getStatusBarConfig())
            .map(StatusBarConfig::getAssetUsageWorkspaces)
            .filter(Predicate.not(List::isEmpty))
            .map(workspaces -> getReferencesToWorkspacesCount(item, workspaces))
            .map(count -> " (" + count + ")")
            .orElse("");
    }


    /**
     * Counts the number of references to the given item across the specified workspaces.
     * Uses JCR SQL2 queries to search for nodes containing references to the item's UUID.
     *
     * @param item the JCR node to count references for
     * @param workspaces the list of workspace names to search in
     * @return the total number of references found across all workspaces
     */
    protected int getReferencesToWorkspacesCount(JcrNodeWrapper item, List<String> workspaces) {
        int size = 0;

        String identifier = NodeUtil.getNodeIdentifierIfPossible(item);

        String statement = String.format("SELECT * FROM [nt:base] AS base WHERE CONTAINS(base.*, 'jcr:%s')", identifier);

        for (String workspace : workspaces) {
            try {
                Session session = MgnlContext.getJCRSession(workspace);
                QueryManager manager = session.getWorkspace().getQueryManager();
                Query query = manager.createQuery(statement, JCR_SQL2);
                QueryResult result = query.execute();
                NodeIterator nodeIterator = result.getNodes();
                // must convert to collection cause .getSize() returns -1
                int iteratorSize = NodeUtil.getCollectionFromNodeIterator(nodeIterator).size();
                size += iteratorSize;

                LOGGER.debug("Found {} references for node {} in {}.", iteratorSize, identifier, workspace);
            } catch (RepositoryException e) {
                LOGGER.warn("Error on getting references by node {}.", identifier, e);
            }
        }

        return size;
    }
}
