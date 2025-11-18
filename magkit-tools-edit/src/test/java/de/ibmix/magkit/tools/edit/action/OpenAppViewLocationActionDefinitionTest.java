package de.ibmix.magkit.tools.edit.action;

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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link OpenAppViewLocationActionDefinition}.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class OpenAppViewLocationActionDefinitionTest {

    @Test
    void testDefaultViewType() {
        OpenAppViewLocationActionDefinition definition = new OpenAppViewLocationActionDefinition();

        assertEquals(OpenAppViewLocationAction.TREE_VIEW, definition.getViewType());
    }

    @Test
    void testSetViewType() {
        OpenAppViewLocationActionDefinition definition = new OpenAppViewLocationActionDefinition();
        String customViewType = "listview";

        definition.setViewType(customViewType);

        assertEquals(customViewType, definition.getViewType());
    }

    @Test
    void testSetViewTypeNull() {
        OpenAppViewLocationActionDefinition definition = new OpenAppViewLocationActionDefinition();

        definition.setViewType(null);

        assertNull(definition.getViewType());
    }

    @Test
    void testSetViewTypeEmpty() {
        OpenAppViewLocationActionDefinition definition = new OpenAppViewLocationActionDefinition();
        String emptyViewType = "";

        definition.setViewType(emptyViewType);

        assertEquals(emptyViewType, definition.getViewType());
    }
}

