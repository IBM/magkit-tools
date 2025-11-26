package de.ibmix.magkit.tools.edit.setup;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
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

import info.magnolia.module.ModuleLifecycleContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link EditToolsModule} covering default values, setters and null handling.
 *
 * @author wolf.bubenik
 * @since 2025-11-19
 */
public class EditToolsModuleTest {

    private final EditToolsModule _module = new EditToolsModule();

    /**
     * Verifies the default state of a freshly constructed module.
     */
    @Test
    public void testDefaultState() {
        assertNotNull(_module.getStatusBarConfig());
        assertNull(_module.getPublicLinkConfig());
        assertEquals(Collections.emptyList(), _module.getMoveConfirmWorkspaces());
    }

    /**
     * Verifies setting and retrieving the public link configuration.
     */
    @Test
    public void testSetPublicLinkConfig() {
        PublicLinkConfig publicLinkConfig = new PublicLinkConfig();
        publicLinkConfig.setExtendedLinkGeneration(true);
        _module.setPublicLinkConfig(publicLinkConfig);
        assertSame(publicLinkConfig, _module.getPublicLinkConfig());
        assertTrue(_module.getPublicLinkConfig().isExtendedLinkGeneration());
    }

    /**
     * Verifies move confirm workspaces logic for set and null handling.
     */
    @Test
    public void testMoveConfirmWorkspaces() {
        List<String> workspaces = Arrays.asList("website", "assets");
        _module.setMoveConfirmWorkspaces(workspaces);
        assertEquals(workspaces, _module.getMoveConfirmWorkspaces());
        _module.setMoveConfirmWorkspaces(null);
        assertEquals(Collections.emptyList(), _module.getMoveConfirmWorkspaces());
    }

    /**
     * Verifies status bar config can be replaced.
     */
    @Test
    public void testStatusBarConfigSetter() {
        StatusBarConfig newConfig = new StatusBarConfig();
        newConfig.setAssetUsageWorkspaces(Arrays.asList("website", "assets"));
        _module.setStatusBarConfig(newConfig);
        assertSame(newConfig, _module.getStatusBarConfig());
        assertEquals(Arrays.asList("website", "assets"), _module.getStatusBarConfig().getAssetUsageWorkspaces());
    }

    /**
     * Verifies start and stop do not throw exceptions.
     */
    @Test
    public void testLifecycleMethodsDoNotThrow() {
        ModuleLifecycleContext context = mock(ModuleLifecycleContext.class);
        assertDoesNotThrow(() -> _module.start(context));
        assertDoesNotThrow(() -> _module.stop(context));
    }
}
