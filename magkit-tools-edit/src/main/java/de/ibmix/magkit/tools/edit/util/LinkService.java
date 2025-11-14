package de.ibmix.magkit.tools.edit.util;

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

import de.ibmix.magkit.tools.edit.setup.EditToolsModule;
import de.ibmix.magkit.tools.edit.setup.PublicLinkConfig;
import info.magnolia.context.MgnlContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.link.LinkUtil;
import info.magnolia.module.site.SiteManager;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.Map;

import static de.ibmix.magkit.core.utils.NodeUtils.IS_PAGE;
import static de.ibmix.magkit.core.utils.NodeUtils.getAncestorOrSelf;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.remove;
import static org.apache.commons.lang.StringUtils.substringAfter;

/**
 * Service providing utility methods for generating links within the Magnolia CMS environment.
 * This service supports creating links to the page editor and public page URLs with optional site-specific host mapping.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Generate direct links to the page editor on the author instance</li>
 * <li>Create public links for pages with context path handling</li>
 * <li>Support extended link generation with site-specific host configuration</li>
 * <li>Integration with Magnolia's site management and configuration properties</li>
 * </ul>
 *
 * <p><strong>Configuration:</strong></p>
 * <ul>
 * <li>Requires Magnolia configuration property "magnolia.author.basePath" for page editor links</li>
 * <li>Optional extended link generation via {@link PublicLinkConfig} with site-to-host mappings</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * // In FreeMarker template (public instance):
 * [#assign pageEditorLink = createPageEditorLink()!]
 * [#if cmsfn.publicInstance &amp;&amp; pageEditorLink?has_content]
 *   &lt;a href="${pageEditorLink}" target="_blank"&gt;EDIT mode on author&lt;/a&gt;
 * [/#if]
 * </pre>
 *
 * @author Oliver Emke
 * @since 2014-12-11
 */
public class LinkService {
    private MagnoliaConfigurationProperties _magnoliaConfigurationProperties;
    private SiteManager _siteManager;
    private EditToolsModule _editToolsModule;

    private String _authorBasePath;

    /**
     * Creates a direct link to the Magnolia page editor for the current page.
     * This method requires the "magnolia.author.basePath" property to be configured as a system property.
     *
     * @return the complete URL to the page editor on the author instance, or an empty string if the base path is not configured or no main content node is available
     */
    @SuppressWarnings("unused")
    public String createPageEditorLink() {
        String editorLink = "";
        if (isNotEmpty(getAuthorBasePath())) {
            Node mainNode = MgnlContext.getAggregationState().getMainContentNode();
            if (mainNode != null) {
                editorLink = getAuthorBasePath() + "/.magnolia/admincentral#app:pages:detail;" + getPathIfPossible(mainNode) + ":edit";
            }
        }
        return editorLink;
    }

    /**
     * Generates the public URL for a given JCR node. The method locates the page ancestor of the node and creates
     * an external link with optional site-specific host mapping when extended link generation is enabled.
     *
     * @param node the JCR node for which to create the public link (may be null)
     * @return the public URL for the node, or an empty string if the node is null
     */
    public String getPublicLink(final Node node) {
        String url = "";

        if (node != null) {
            final String contextPath = MgnlContext.getContextPath();
            final Node page = getAncestorOrSelf(node, IS_PAGE);

            url = createExternalLink(page);
            if (isNotEmpty(contextPath)) {
                url = remove(url, contextPath);
            }

            final PublicLinkConfig publicLinkConfig = _editToolsModule.getPublicLinkConfig();
            if (publicLinkConfig != null && publicLinkConfig.isExtendedLinkGeneration()) {
                final Map<String, String> siteHosts = publicLinkConfig.getSiteHosts();
                final String siteName = _siteManager.getAssignedSite(node).getName();
                if (siteHosts.containsKey(siteName)) {
                    url = siteHosts.get(siteName) + "/" + substringAfter(substringAfter(url, "://"), "/");
                }
            }
        }

        return url;
    }

    private String getAuthorBasePath() {
        if (_authorBasePath == null) {
            _authorBasePath = defaultString(_magnoliaConfigurationProperties.getProperty("magnolia.author.basePath"));
        }
        return _authorBasePath;
    }

    /**
     * Creates an external link for the given node using Magnolia's LinkUtil.
     *
     * @param node the JCR node for which to create the external link
     * @return the external link as a string
     */
    protected String createExternalLink(final Node node) {
        return LinkUtil.createExternalLink(node);
    }

    /**
     * Sets the edit tools module for accessing public link configuration.
     *
     * @param editToolsModule the edit tools module instance
     */
    @Inject
    public void setEditToolsModule(final EditToolsModule editToolsModule) {
        _editToolsModule = editToolsModule;
    }

    /**
     * Sets the site manager for site-specific link generation.
     *
     * @param siteManager the site manager instance
     */
    @Inject
    public void setSiteManager(final SiteManager siteManager) {
        _siteManager = siteManager;
    }

    /**
     * Sets the Magnolia configuration properties for accessing system configuration.
     *
     * @param magnoliaConfigurationProperties the configuration properties instance
     */
    @Inject
    public void setMagnoliaConfigurationProperties(final MagnoliaConfigurationProperties magnoliaConfigurationProperties) {
        _magnoliaConfigurationProperties = magnoliaConfigurationProperties;
    }

}
