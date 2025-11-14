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
 * Configuration bean for status bar enhancements in the Magnolia AdminCentral.
 * This configuration controls which workspaces are searched for asset usage references
 * and which workspaces display UUID information in the status bar.
 *
 * <p><strong>Configuration Properties:</strong></p>
 * <ul>
 * <li>assetUsageWorkspaces - Workspaces to search for asset references (default: website workspace)</li>
 * <li>showUuidWorkspaces - Workspaces where UUIDs are displayed in the status bar (default: website workspace)</li>
 * </ul>
 *
 * <p><strong>Default Configuration:</strong></p>
 * Both properties default to searching only the "website" workspace to minimize performance impact.
 *
 * @author frank.sommer
 * @since 2015-12-01
 */
public class StatusBarConfig {
    private List<String> _assetUsageWorkspaces = Collections.singletonList(WEBSITE);
    private List<String> _showUuidWorkspaces = Collections.singletonList(WEBSITE);

    /**
     * Returns the list of workspaces to search for asset usage references.
     *
     * @return the list of workspace names
     */
    public List<String> getAssetUsageWorkspaces() {
        return _assetUsageWorkspaces;
    }

    /**
     * Sets the list of workspaces to search for asset usage references.
     *
     * @param assetUsageWorkspaces the list of workspace names
     */
    public void setAssetUsageWorkspaces(final List<String> assetUsageWorkspaces) {
        _assetUsageWorkspaces = assetUsageWorkspaces;
    }

    /**
     * Returns the list of workspaces where UUIDs should be displayed in the status bar.
     *
     * @return the list of workspace names
     */
    public List<String> getShowUuidWorkspaces() {
        return _showUuidWorkspaces;
    }

    /**
     * Sets the list of workspaces where UUIDs should be displayed in the status bar.
     *
     * @param showUuidWorkspaces the list of workspace names
     */
    public void setShowUuidWorkspaces(List<String> showUuidWorkspaces) {
        _showUuidWorkspaces = showUuidWorkspaces;
    }
}
