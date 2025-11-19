package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * magkit-tools-scheduler
 * %%
 * Copyright (C) 2025 IBM iX
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

import info.magnolia.ui.contentapp.column.jcr.JcrTitleColumnDefinition;
import org.junit.jupiter.api.Test;

import javax.jcr.Item;
import javax.jcr.Node;
import java.util.HashMap;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JobNodeTitleProvider}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-19
 */
class JobNodeTitleProviderTest {

    /**
     * Verifies apply returns icon fragment plus node name for a regular node.
     */
    @Test
    void applyReturnsIconAndNameForRegularNode() throws Exception {
        JcrTitleColumnDefinition def = new JcrTitleColumnDefinition();
        def.setNodeTypeToIcon(new HashMap<>());
        JobNodeTitleProvider provider = new JobNodeTitleProvider(def);
        Item item = mockNode("jobs", "/path/job");
        assertEquals("<span class=\"v-table-icon-element icon-node-content\" ></span>job", provider.apply(item));
    }

    /**
     * Verifies apply returns empty string for a node named params.
     */
    @Test
    void applyReturnsEmptyStringForParamsNode() throws Exception {
        JcrTitleColumnDefinition def = new JcrTitleColumnDefinition();
        JobNodeTitleProvider provider = new JobNodeTitleProvider(def);
        Item item = mockNode("jobs", "/path/params");
        assertEquals("", provider.apply(item));
    }

    /**
     * Verifies apply returns empty string for non-node items.
     */
    @Test
    void applyReturnsEmptyStringForNonNodeItem() {
        JcrTitleColumnDefinition def = new JcrTitleColumnDefinition();
        JobNodeTitleProvider provider = new JobNodeTitleProvider(def);
        Item item = mock(Item.class);
        when(item.isNode()).thenReturn(false);
        assertEquals("", provider.apply(item));
    }

    /**
     * Verifies getIcon returns mapped icon for known node type.
     */
    @Test
    void getIconReturnsMappedIconForNodeType() throws Exception {
        JcrTitleColumnDefinition def = new JcrTitleColumnDefinition();
        HashMap<String, String> mapping = new HashMap<>();
        mapping.put("mgnl:job", "mapped-icon");
        def.setNodeTypeToIcon(mapping);
        JobNodeTitleProvider provider = new JobNodeTitleProvider(def);
        Node node = mockMgnlNode("jobs", "/path/job", "mgnl:job");

        assertEquals("mapped-icon", provider.getIcon(node));
    }

    /**
     * Verifies getIcon returns empty fallback when node type not mapped.
     */
    @Test
    void getIconReturnsFallbackIconWhenTypeNotMapped() throws Exception {
        JcrTitleColumnDefinition def = new JcrTitleColumnDefinition();
        JobNodeTitleProvider provider = new JobNodeTitleProvider(def);
        Node node = mockMgnlNode("jobs", "/path/job", "mgnl:unknown");

        assertEquals("icon-node-content", provider.getIcon(node));
    }
}
