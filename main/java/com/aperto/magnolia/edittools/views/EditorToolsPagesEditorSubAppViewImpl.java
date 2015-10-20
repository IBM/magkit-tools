package com.aperto.magnolia.edittools.views;

import com.aperto.magnolia.edittools.util.LinkService;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Link;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.pages.app.editor.PagesEditorSubAppViewImpl;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;
import info.magnolia.ui.workbench.StatusBarView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.apache.commons.lang.StringUtils.isNotBlank;

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
    private final LinkService _linkService;

    @Inject
    public EditorToolsPagesEditorSubAppViewImpl(final PageBarView pageBarView, final SubAppContext subAppContext, final SimpleTranslator i18n, final ServerConfiguration serverConfiguration, final LinkService linkService) {
        super();
        _subAppContext = subAppContext;
        _i18n = i18n;
        _linkService = linkService;
    }

    @Override
    public void setStatusBarView(final StatusBarView statusBarView) {
        LOGGER.debug("Set the status bar.");
        super.setStatusBarView(statusBarView);

        // public link is the second component, only add if not added before
        if (((AbstractOrderedLayout) statusBarView.asVaadinComponent()).getComponentCount() < 2) {
            final DetailLocation location = DetailLocation.wrap(_subAppContext.getLocation());
            final String nodePath = location.getNodePath();
            final String publicUrl = _linkService.getPublicLink(nodePath);
            if (isNotBlank(publicUrl)) {
                Link publicLink = new Link(_i18n.translate("statusbar.view.on.public"), new ExternalResource(publicUrl));
                publicLink.setTargetName("_blank");
                statusBarView.addComponent(publicLink, Alignment.MIDDLE_RIGHT);
            }
        }
    }
}
