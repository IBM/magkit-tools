package com.aperto.magnolia.edittools.export;

import info.magnolia.importexport.command.JcrExportCommand;
import info.magnolia.jcr.predicate.NodeFilteringPredicate;
import info.magnolia.jcr.util.NodeUtil;

import javax.jcr.Node;

/**
 * Single page node filter.
 *
 * @author frank.sommer
 * @since 15.09.2023
 */
public class SinglePageNodeFilter extends JcrExportCommand.DefaultFilter {

    @Override
    public Node wrapNode(Node node) {
        final NodeFilteringPredicate nodePredicate = getNodePredicate();
        if (nodePredicate instanceof SinglePageNodeFilteringPredicate) {
            String nodePath = NodeUtil.getPathIfPossible(node);
            ((SinglePageNodeFilteringPredicate) nodePredicate).setBasePageNodePath(nodePath);
        }
        setNodePredicate(nodePredicate);

        return super.wrapNode(node);
    }
}
