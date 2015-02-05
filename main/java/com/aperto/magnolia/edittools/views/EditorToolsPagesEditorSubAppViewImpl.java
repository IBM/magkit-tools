package com.aperto.magnolia.edittools.views;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Link;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.link.LinkUtil;
import info.magnolia.pages.app.editor.PagesEditorSubAppViewImpl;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.workbench.StatusBarView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;

import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.remove;

/**
 * Extends the status bar of the pages editor sub app.
 *
 * @author frank.sommer
 * @since 08.05.14
 */
public class EditorToolsPagesEditorSubAppViewImpl extends PagesEditorSubAppViewImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditorToolsPagesEditorSubAppViewImpl.class);

    private static final long serialVersionUID = 8175070407228549735L;

    private final transient SubAppContext _subAppContext;
    private final SimpleTranslator _i18n;

    @Inject
    public EditorToolsPagesEditorSubAppViewImpl(final PageBarView pageBarView, final SubAppContext subAppContext, final SimpleTranslator i18n, final ServerConfiguration serverConfiguration, final ActivationManager activationManager) {
        super(pageBarView, subAppContext, i18n, serverConfiguration);
        _subAppContext = subAppContext;
        _i18n = i18n;
    }

    @Override
    public void setStatusBarView(final StatusBarView statusBarView) {
        LOGGER.debug("Set the status bar.");
        super.setStatusBarView(statusBarView);

        // public link is the second component, only add if not added before
        if (((AbstractOrderedLayout) statusBarView.asVaadinComponent()).getComponentCount() < 2) {
            Link publicLink = new Link(_i18n.translate("statusbar.view.on.public"), new ExternalResource(getPublicLink()));
            publicLink.setTargetName("_blank");
            statusBarView.addComponent(publicLink, Alignment.MIDDLE_RIGHT);
        }
    }

    /**
     * Gets the link from public.
     */
    private String getPublicLink() {
        final DetailLocation location = DetailLocation.wrap(_subAppContext.getLocation());
        final String contextPath = MgnlContext.getContextPath();

        final String nodePath = location.getNodePath();
        final Node node = SessionUtil.getNode(WEBSITE, nodePath);
        String url = createExternalLink(node);
        if (isNotEmpty(contextPath)) {
            url = remove(url, contextPath);
        }
        return url;
    }

    private String createExternalLink(final Node node) {
        String link = "";
        if (node != null) {
            link = LinkUtil.createExternalLink(node);
        }
        return link;
    }


}
