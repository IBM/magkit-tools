package com.aperto.magnolia.edittools.setup;

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
