package de.ibmix.magkit.tools.edit.export;

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

import info.magnolia.jcr.predicate.NodeFilteringPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Predicate for filtering JCR nodes during export operations to include only a single page and its descendants.
 * This predicate extends {@link NodeFilteringPredicate} and adds logic to ensure that only nodes within
 * a specified base page path are evaluated as true.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Filters nodes based on a configured base page node path</li>
 * <li>Handles page variant nodes by checking the origin node path</li>
 * <li>Ensures only the target page and its descendants are included in export operations</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * Set the base page node path using {@link #setBasePageNodePath(String)} before evaluation.
 * The predicate will then filter nodes to include only those within or equal to the base page path.
 *
 * <p><strong>Special Handling:</strong></p>
 * Page variant nodes (nodes with "mgnl:variant" mixin) are evaluated by checking their origin node
 * (grandparent) path instead of their actual path.
 *
 * @author frank.sommer
 * @since 2023-09-15
 */
public class SinglePageNodeFilteringPredicate extends NodeFilteringPredicate {
    private static final Logger LOGGER = LoggerFactory.getLogger(SinglePageNodeFilteringPredicate.class);

    private String _basePageNodePath;

    /**
     * Sets the base page node path used for filtering decisions.
     *
     * @param basePageNodePath the JCR path of the base page node
     */
    public void setBasePageNodePath(String basePageNodePath) {
        _basePageNodePath = basePageNodePath;
    }

    /**
     * Evaluates whether a node should be included based on the base page node path.
     * For page nodes, checks if the base page path starts with the node's origin path,
     * handling page variant nodes specially.
     *
     * @param node the node to evaluate
     * @return true if the node should be included, false otherwise
     */
    @Override
    public boolean evaluateTyped(Node node) {
        boolean evaluated = super.evaluateTyped(node);
        try {
            if (evaluated && NodeUtil.isNodeType(node, NodeTypes.Page.NAME)) {
                String originNodePath = determineOriginNodePath(node);

                // info.magnolia.jcr.decoration.NodePredicateContentDecorator.evaluateNode checks current and ancestor nodes
                evaluated = _basePageNodePath.startsWith(originNodePath);
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error evaluate node type.", e);
        }
        return evaluated;
    }

    /**
     * Check for page variant nodes.
     */
    private static String determineOriginNodePath(Node node) throws RepositoryException {
        String originNodePath = node.getPath();
        // node is a page variant
        if (NodeUtil.hasMixin(node, "mgnl:variant")) {
            // the grandparent node should be the origin node
            originNodePath = node.getParent().getParent().getPath();
        }
        return originNodePath;
    }
}
