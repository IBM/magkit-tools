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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StatusBarConfig} verifying defaults and setter behaviour including null handling.
 *
 * @author wolf.bubenik
 * @since 2025-11-19
 */
public class StatusBarConfigTest {

    private final StatusBarConfig _config = new StatusBarConfig();

    /**
     * Verifies default workspace values.
     */
    @Test
    public void testDefaultWorkspaces() {
        assertEquals(List.of(WEBSITE), _config.getAssetUsageWorkspaces());
        assertEquals(List.of(WEBSITE), _config.getShowUuidWorkspaces());
    }

    /**
     * Verifies setter mutation for asset usage workspaces.
     */
    @Test
    public void testSetAssetUsageWorkspaces() {
        List<String> workspaces = Arrays.asList("website", "assets");
        _config.setAssetUsageWorkspaces(workspaces);
        assertEquals(workspaces, _config.getAssetUsageWorkspaces());
        _config.setAssetUsageWorkspaces(null);
        assertNull(_config.getAssetUsageWorkspaces());
    }

    /**
     * Verifies setter mutation for show uuid workspaces.
     */
    @Test
    public void testSetShowUuidWorkspaces() {
        List<String> workspaces = Arrays.asList("website", "contacts");
        _config.setShowUuidWorkspaces(workspaces);
        assertEquals(workspaces, _config.getShowUuidWorkspaces());
        _config.setShowUuidWorkspaces(null);
        assertNull(_config.getShowUuidWorkspaces());
    }
}
