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
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link OpenPageBrowserLocationActionDefinition}.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class OpenPageBrowserLocationActionDefinitionTest {

    @Test
    void testImplementationClass() {
        OpenPageBrowserLocationActionDefinition definition = new OpenPageBrowserLocationActionDefinition();

        assertEquals(OpenPageBrowserLocationAction.class, definition.getImplementationClass());
    }

    @Test
    void testDefaultViewType() {
        OpenPageBrowserLocationActionDefinition definition = new OpenPageBrowserLocationActionDefinition();

        assertNotNull(definition.getViewType());
        assertEquals(OpenAppViewLocationAction.TREE_VIEW, definition.getViewType());
    }
}

