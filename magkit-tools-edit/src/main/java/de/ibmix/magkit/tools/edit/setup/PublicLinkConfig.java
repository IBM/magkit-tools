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
 * Public link config.
 *
 * @author frank.sommer
 * @since 09.07.2015
 */
public class PublicLinkConfig {
    private boolean _extendedLinkGeneration;
    private Map<String, String> _siteHosts = new HashMap<>();

    public boolean isExtendedLinkGeneration() {
        return _extendedLinkGeneration;
    }

    public void setExtendedLinkGeneration(final boolean extendedLinkGeneration) {
        _extendedLinkGeneration = extendedLinkGeneration;
    }

    public Map<String, String> getSiteHosts() {
        return _siteHosts;
    }

    public void setSiteHosts(final Map<String, String> siteHosts) {
        _siteHosts = siteHosts;
    }
}
