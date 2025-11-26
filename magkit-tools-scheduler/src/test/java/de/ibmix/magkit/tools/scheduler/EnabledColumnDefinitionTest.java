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

import com.vaadin.ui.renderers.HtmlRenderer;
import de.ibmix.magkit.test.jcr.SessionMockUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Item;
import javax.jcr.Node;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EnabledColumnDefinition.TickValueProvider}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-19
 */
class EnabledColumnDefinitionTest {

    @AfterEach
    void tearDown() {
        SessionMockUtils.cleanSession();
    }

    @Test
    void constructor() {
        EnabledColumnDefinition def = new EnabledColumnDefinition();
        assertEquals(HtmlRenderer.class, def.getRenderer());
        assertEquals(EnabledColumnDefinition.TickValueProvider.class, def.getValueProvider());
    }

    /**
     * Verifies that apply returns the span HTML when the node has enabled=true.
     */
    @Test
    void applyReturnsSpanForEnabledNode() throws Exception {
        Item item = mockNode("jobs", "/path/job", stubProperty("enabled", true));
        EnabledColumnDefinition.TickValueProvider provider = new EnabledColumnDefinition.TickValueProvider();

        assertEquals("<span class=\"v-table-icon-element icon-tick\" ></span>", provider.apply(item));
    }

    /**
     * Verifies apply returns null for a node with enabled=false.
     */
    @Test
    void applyReturnsNullForDisabledNode() throws Exception {
        Item item = mockNode("jobs", "/path/job", stubProperty("enabled", false));
        EnabledColumnDefinition.TickValueProvider provider = new EnabledColumnDefinition.TickValueProvider();

        assertNull(provider.apply(item));
    }

    /**
     * Verifies apply returns null when the enabled property is absent.
     */
    @Test
    void applyReturnsNullForNodeWithoutProperty() throws Exception {
        Item item = mockNode("jobs", "/path/job");
        EnabledColumnDefinition.TickValueProvider provider = new EnabledColumnDefinition.TickValueProvider();

        assertNull(provider.apply(item));
    }

    /**
     * Verifies apply returns null for a non-node item.
     */
    @Test
    void applyReturnsNullForNonNodeItem() {
        Item item = mock(Item.class);
        when(item.isNode()).thenReturn(false);
        EnabledColumnDefinition.TickValueProvider provider = new EnabledColumnDefinition.TickValueProvider();

        assertNull(provider.apply(item));
    }

    /**
     * Verifies getIcon returns icon-tick when enabled property is true.
     */
    @Test
    void getIconReturnsIconTickForEnabledNode() throws Exception {
        Node node = mockNode("jobs", "/path/job", stubProperty("enabled", true));
        EnabledColumnDefinition.TickValueProvider provider = new EnabledColumnDefinition.TickValueProvider();

        assertEquals("icon-tick", provider.getIcon(node));
    }

    /**
     * Verifies getIcon returns empty string when enabled property is false.
     */
    @Test
    void getIconReturnsEmptyForDisabledNode() throws Exception {
        Node node = mockNode("jobs", "/path/job", stubProperty("enabled", false));
        EnabledColumnDefinition.TickValueProvider provider = new EnabledColumnDefinition.TickValueProvider();

        assertEquals("", provider.getIcon(node));
    }
}
