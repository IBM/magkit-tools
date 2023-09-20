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
