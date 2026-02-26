package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * magkit-scheduler
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

import com.machinezoo.noexception.Exceptions;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.contentapp.column.jcr.JcrTitleColumnDefinition;
import jakarta.inject.Inject;

import javax.jcr.Item;
import javax.jcr.Node;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Custom title provider for scheduler job nodes that filters out parameter nodes from display.
 * This provider extends the standard JCR title column provider to customize the display of job definitions
 * in content app grids by hiding the "params" child nodes.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Displays node names for all job-related nodes</li>
 * <li>Filters out nodes named "params" to reduce clutter in the UI</li>
 * <li>Provides custom icon mapping based on node types</li>
 * <li>Returns empty string for filtered nodes instead of null</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * Configured as a value provider for title columns in scheduler content app definitions
 * to improve the visual presentation of job hierarchies.
 *
 * @author frank.sommer
 * @see JcrTitleColumnDefinition.JcrTitleValueProvider
 * @since 2023-09-01
 */
public class JobNodeTitleProvider extends JcrTitleColumnDefinition.IconValueProvider<Item, JcrTitleColumnDefinition> {

    /**
     * Creates a new JobNodeTitleProvider with the given column definition.
     *
     * @param definition the JCR title column definition containing configuration
     */
    @Inject
    JobNodeTitleProvider(JcrTitleColumnDefinition definition) {
        super(definition);
    }

    /**
     * Provides the display value for a JCR item in the title column.
     * Filters out nodes named "params" and returns an empty string for them.
     *
     * @param item the JCR item to get the display value for
     * @return the display value combining icon and node name, or empty string for filtered nodes
     */
    @Override
    public String apply(Item item) {
        String displayName = null;
        if (item.isNode()) {
            String nodeName = NodeUtil.getName((Node) item);
            displayName = !"params".equals(nodeName) ? nodeName : null;
        }
        return displayName != null ? super.apply(item) + displayName : EMPTY;
    }

    /**
     * Determines the icon to display for the given JCR item based on its node type.
     * Uses the node type to icon mapping from the column definition.
     *
     * @param item the JCR item to get the icon for
     * @return the icon CSS class name, or empty string if item is not a node
     */
    @Override
    protected String getIcon(Item item) {
        return Optional.of(item)
            .filter(Item::isNode)
            .map(Node.class::cast)
            .map(Exceptions.wrap().function(node -> getDefinition().getNodeTypeToIcon().getOrDefault(node.getPrimaryNodeType().getName(), super.getIcon(item))))
            .orElse(EMPTY);
    }
}
