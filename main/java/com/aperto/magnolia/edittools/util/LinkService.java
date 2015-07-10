package com.aperto.magnolia.edittools.util;

import com.aperto.magnolia.edittools.setup.EditToolsModule;
import com.aperto.magnolia.edittools.setup.PublicLinkConfig;
import info.magnolia.context.MgnlContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.link.LinkUtil;
import info.magnolia.module.templatingkit.sites.SiteManager;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.Map;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang.StringUtils.*;

/**
 * Useful link methods.
 *
 * @author Oliver Emke
 * @since 11.12.14
 */
public class LinkService {
    private MagnoliaConfigurationProperties _magnoliaConfigurationProperties;
    private SiteManager _siteManager;
    private EditToolsModule _editToolsModule;

    private String _authorBasePath;

    /**
     * Creates a link directly to the page editor if property "magnolia.author.basePath" was set as tomcat variable.
     * <p/>
     * Used in ftl-Templates on public:
     * [#assign pageEditorLink = createPageEditorLink()!]
     * [#if cmsfn.publicInstance && pageEditorLink?has_content]
     * <a href="${pageEditorLink}" style="color: #FF9900" target="_blank">EDIT mode on author</a>
     * [/#if]
     *
     * @return page editor link
     */
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
     * Gets the public link for a node.
     *
     * @param nodePath node path for link creation
     */
    public String getPublicLink(final String nodePath) {
        String url = "";
        final Node node = SessionUtil.getNode(WEBSITE, nodePath);

        if (node != null) {
            final String contextPath = MgnlContext.getContextPath();
            url = createExternalLink(node);
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

    protected String createExternalLink(final Node node) {
        return LinkUtil.createExternalLink(node);
    }

    @Inject
    public void setEditToolsModule(final EditToolsModule editToolsModule) {
        _editToolsModule = editToolsModule;
    }

    @Inject
    public void setSiteManager(final SiteManager siteManager) {
        _siteManager = siteManager;
    }

    @Inject
    public void setMagnoliaConfigurationProperties(final MagnoliaConfigurationProperties magnoliaConfigurationProperties) {
        _magnoliaConfigurationProperties = magnoliaConfigurationProperties;
    }
}