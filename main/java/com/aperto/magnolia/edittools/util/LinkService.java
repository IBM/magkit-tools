package com.aperto.magnolia.edittools.util;

import info.magnolia.context.MgnlContext;
import info.magnolia.init.MagnoliaConfigurationProperties;

import javax.inject.Inject;
import javax.jcr.Node;

import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Useful link methods.
 *
 * @author Oliver Emke
 * @since 11.12.14
 */
public class LinkService {

    @Inject
    private MagnoliaConfigurationProperties _magnoliaConfigurationProperties;

    private String _authorBasePath;

    /**
     * Creates a link directly to the page editor if property "magnolia.author.basePath" was set as tomcat variable.
     *
     * Used in ftl-Templates on public:
     * [#assign pageEditorLink = createPageEditorLink()!]
     * [#if cmsfn.publicInstance && pageEditorLink?has_content]
     *  <a href="${pageEditorLink}" style="color: #FF9900" target="_blank">EDIT mode on author</a>
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

    private String getAuthorBasePath() {
        if (_authorBasePath == null) {
            _authorBasePath = defaultString(_magnoliaConfigurationProperties.getProperty("magnolia.author.basePath"));
        }
        return _authorBasePath;
    }
}