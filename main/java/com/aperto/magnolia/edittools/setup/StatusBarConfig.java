package com.aperto.magnolia.edittools.setup;

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
