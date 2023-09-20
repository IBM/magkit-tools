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

import java.util.Collections;
import java.util.List;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;

/**
 * Status bar config bean.
 *
 * @author frank.sommer
 * @since 01.12.2015
 */
public class StatusBarConfig {
    private List<String> _assetUsageWorkspaces = Collections.singletonList(WEBSITE);
    private List<String> _showUuidWorkspaces = Collections.singletonList(WEBSITE);

    public List<String> getAssetUsageWorkspaces() {
        return _assetUsageWorkspaces;
    }

    public void setAssetUsageWorkspaces(final List<String> assetUsageWorkspaces) {
        _assetUsageWorkspaces = assetUsageWorkspaces;
    }

    public List<String> getShowUuidWorkspaces() {
        return _showUuidWorkspaces;
    }

    public void setShowUuidWorkspaces(List<String> showUuidWorkspaces) {
        _showUuidWorkspaces = showUuidWorkspaces;
    }
}
