package com.aperto.magnolia.edittools.m6.ui;

import com.aperto.magnolia.edittools.setup.EditToolsModule;
import com.aperto.magnolia.edittools.setup.StatusBarConfig;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.contentapp.DefaultItemDescriber;
import info.magnolia.ui.datasource.jcr.JcrNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
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
 * Describer describes asset usage in statusbar.
 *
 * @author Philipp Güttler (Aperto GmbH)
 * @since 11.03.2021
 */
public class AssetUsageItemDescriber extends DefaultItemDescriber<JcrNodeWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetUsageItemDescriber.class);

    private final Provider<EditToolsModule> _editToolsModule;

    @Inject
    public AssetUsageItemDescriber(Provider<EditToolsModule> editToolsModule) {
        _editToolsModule = editToolsModule;
    }

    @Override
    public String apply(final Collection<JcrNodeWrapper> items) {
        return items.size() == 1 ? applySingle(items.iterator().next()) : super.apply(items);
    }

    public String applySingle(final JcrNodeWrapper item) {
        return NodeUtil.getPathIfPossible(item) + Optional.ofNullable(_editToolsModule.get().getStatusBarConfig())
            .map(StatusBarConfig::getAssetUsageWorkspaces)
            .filter(Predicate.not(List::isEmpty))
            .map(workspaces -> getReferencesToWorkspacesCount(item, workspaces))
            .map(count -> " (" + count + ")")
            .orElse("");
    }


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
