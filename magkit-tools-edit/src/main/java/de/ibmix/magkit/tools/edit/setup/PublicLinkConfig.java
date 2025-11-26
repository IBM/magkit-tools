package de.ibmix.magkit.tools.edit.setup;

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

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration bean for public link generation with site-specific host mappings.
 * This configuration enables extended link generation that maps different sites to specific host URLs,
 * useful for multi-site Magnolia installations where each site has its own public domain.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Enable/disable extended link generation with site-specific host mapping</li>
 * <li>Configure host URLs per site name</li>
 * <li>Support multi-site setups with different public domains</li>
 * </ul>
 *
 * <p><strong>Configuration Example:</strong></p>
 * <pre>
 * publicLinkConfig:
 *   extendedLinkGeneration: true
 *   siteHosts:
 *     site1: https://www.example.com
 *     site2: https://www.another-domain.com
 * </pre>
 *
 * @author frank.sommer
 * @since 2015-07-09
 */
public class PublicLinkConfig {
    private boolean _extendedLinkGeneration;
    private Map<String, String> _siteHosts = new HashMap<>();

    /**
     * Checks if extended link generation with site-specific host mapping is enabled.
     *
     * @return true if extended link generation is enabled, false otherwise
     */
    public boolean isExtendedLinkGeneration() {
        return _extendedLinkGeneration;
    }

    /**
     * Enables or disables extended link generation with site-specific host mapping.
     *
     * @param extendedLinkGeneration true to enable extended link generation, false to disable
     */
    public void setExtendedLinkGeneration(final boolean extendedLinkGeneration) {
        _extendedLinkGeneration = extendedLinkGeneration;
    }

    /**
     * Returns the map of site names to their corresponding host URLs.
     *
     * @return a map where keys are site names and values are host URLs
     */
    public Map<String, String> getSiteHosts() {
        return _siteHosts;
    }

    /**
     * Sets the map of site names to their corresponding host URLs.
     *
     * @param siteHosts a map where keys are site names and values are host URLs
     */
    public void setSiteHosts(final Map<String, String> siteHosts) {
        _siteHosts = siteHosts;
    }
}
