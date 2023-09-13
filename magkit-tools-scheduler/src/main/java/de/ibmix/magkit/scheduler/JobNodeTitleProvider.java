package de.ibmix.magkit.scheduler;

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

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Extended node name provider which ignores params content nodes.
 *
 * @author frank.sommer
 * @see JcrTitleColumnDefinition.JcrTitleValueProvider
 * @since 01.09.2023
 */
public class JobNodeTitleProvider extends JcrTitleColumnDefinition.IconValueProvider<Item, JcrTitleColumnDefinition> {

    @Inject
    JobNodeTitleProvider(JcrTitleColumnDefinition definition) {
        super(definition);
    }

    @Override
    public String apply(Item item) {
        String displayName = null;
        if (item.isNode()) {
            String nodeName = NodeUtil.getName((Node) item);
            displayName = !"params".equals(nodeName) ? nodeName : null;
        }
        return displayName != null ? super.apply(item) + displayName : EMPTY;
    }

    @Override
    protected String getIcon(Item item) {
        return Optional.of(item)
            .filter(Item::isNode)
            .map(Node.class::cast)
            .map(Exceptions.wrap().function(node -> getDefinition().getNodeTypeToIcon().getOrDefault(node.getPrimaryNodeType().getName(), super.getIcon(item))))
            .orElse(EMPTY);
    }
}
