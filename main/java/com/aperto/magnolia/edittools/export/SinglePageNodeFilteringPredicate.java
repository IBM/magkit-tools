package com.aperto.magnolia.edittools.export;

import info.magnolia.jcr.predicate.NodeFilteringPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Single page node predicate.
 *
 * @author frank.sommer
 * @since 15.09.2023
 */
public class SinglePageNodeFilteringPredicate extends NodeFilteringPredicate {
    private static final Logger LOGGER = LoggerFactory.getLogger(SinglePageNodeFilteringPredicate.class);

    private String _basePageNodePath;

    public void setBasePageNodePath(String basePageNodePath) {
        _basePageNodePath = basePageNodePath;
    }

    @Override
    public boolean evaluateTyped(Node node) {
        boolean evaluated = super.evaluateTyped(node);
        try {
            if (evaluated && NodeUtil.isNodeType(node, NodeTypes.Page.NAME)) {
                // info.magnolia.jcr.decoration.NodePredicateContentDecorator.evaluateNode checks current and ancestor nodes
                evaluated = _basePageNodePath.startsWith(node.getPath());
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error evaluate node type.", e);
        }
        return evaluated;
    }
}
