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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link PublicLinkConfig} verifying defaults and mutation behaviour.
 *
 * @author wolf.bubenik
 * @since 2025-11-19
 */
public class PublicLinkConfigTest {

    private final PublicLinkConfig _config = new PublicLinkConfig();

    /**
     * Verifies default values after construction.
     */
    @Test
    public void testDefaultValues() {
        assertFalse(_config.isExtendedLinkGeneration());
        assertNotNull(_config.getSiteHosts());
        assertTrue(_config.getSiteHosts().isEmpty());
    }

    /**
     * Verifies enabling extended link generation.
     */
    @Test
    public void testEnableExtendedLinkGeneration() {
        _config.setExtendedLinkGeneration(true);
        assertTrue(_config.isExtendedLinkGeneration());
        _config.setExtendedLinkGeneration(false);
        assertFalse(_config.isExtendedLinkGeneration());
    }

    /**
     * Verifies site host map mutation and null assignment behaviour.
     */
    @Test
    public void testSiteHostsMutationAndNullHandling() {
        Map<String, String> hosts = new HashMap<>();
        hosts.put("site1", "https://example.com");
        _config.setSiteHosts(hosts);
        assertEquals("https://example.com", _config.getSiteHosts().get("site1"));
        _config.setSiteHosts(null);
        assertNull(_config.getSiteHosts());
    }
}
