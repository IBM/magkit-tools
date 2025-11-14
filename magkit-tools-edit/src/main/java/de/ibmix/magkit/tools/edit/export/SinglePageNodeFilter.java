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

import info.magnolia.importexport.command.JcrExportCommand;
import info.magnolia.jcr.predicate.NodeFilteringPredicate;
import info.magnolia.jcr.util.NodeUtil;

import javax.jcr.Node;

/**
 * Node filter for JCR export operations that limits the export to a single page and its descendants.
 * This filter extends {@link info.magnolia.importexport.command.JcrExportCommand.DefaultFilter} and
 * configures the {@link SinglePageNodeFilteringPredicate} with the appropriate base page path.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Configures the node filtering predicate with the base page path during node wrapping</li>
 * <li>Ensures only the selected page and its content are included in export operations</li>
 * <li>Works seamlessly with Magnolia's JCR export command infrastructure</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * Use this filter with Magnolia's export commands when you need to export only a single page
 * instead of the entire content tree.
 *
 * @author frank.sommer
 * @since 2023-09-15
 */
public class SinglePageNodeFilter extends JcrExportCommand.DefaultFilter {

    /**
     * Wraps the node and configures the filtering predicate with the node's path as the base page path.
     *
     * @param node the node to wrap
     * @return the wrapped node with configured filtering
     */
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
