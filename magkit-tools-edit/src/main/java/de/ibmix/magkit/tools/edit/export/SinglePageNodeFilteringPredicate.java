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
