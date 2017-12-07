package com.aperto.magnolia.edittools.ui.workbench;

import com.aperto.magnolia.edittools.setup.EditToolsModule;
import com.aperto.magnolia.edittools.util.LinkService;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.link.LinkException;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.workbench.ContentPresenter;
import info.magnolia.ui.workbench.StatusBarView;
import info.magnolia.ui.workbench.WorkbenchStatusBarPresenter;
import info.magnolia.ui.workbench.event.SelectionChangedEvent;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.List;
import java.util.Set;

import static com.aperto.magkit.utils.NodeUtils.getNodeByIdentifier;
import static com.vaadin.ui.Alignment.TOP_LEFT;
import static com.vaadin.ui.Alignment.TOP_RIGHT;
import static info.magnolia.link.LinkUtil.convertUUIDtoHandle;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static javax.jcr.query.Query.JCR_SQL2;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Workbench status bar presenter with uuid and link to public in status bar.
 *
 * @author Oliver Emke
 * @since 23.02.15
 */
public class ToolsWorkbenchStatusBarPresenter extends WorkbenchStatusBarPresenter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsWorkbenchStatusBarPresenter.class);

    private final StatusBarView _view;
    private ContentConnector _contentConnector;
    private EventBus _eventBus;
    private final Label _selectionLabel = new Label();
    private final Label _rightLabel = new Label();
    private ContentPresenter _activeContentPresenter;
    private boolean _rootIsSelected;
    private SimpleTranslator _i18n;
    private final EditToolsModule _toolsModule;
    private final Link _linkToPublic = new Link();
    private final LinkService _linkService;

    @Inject
    public ToolsWorkbenchStatusBarPresenter(StatusBarView view, ContentConnector contentConnector, SimpleTranslator i18n, final LinkService linkService) {
        super(view, contentConnector, i18n);
        _view = view;
        _contentConnector = contentConnector;
        _i18n = i18n;
        _linkService = linkService;
        _toolsModule = Components.getComponent(EditToolsModule.class);
    }

    private void bindHandlers() {
        _eventBus.addHandler(SelectionChangedEvent.class, new SelectionChangedEvent.Handler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                setSelectedItems(event.getItemIds());
            }
        });
    }

    public StatusBarView start(EventBus eventBus, ContentPresenter activeContentPresenter) {
        _eventBus = eventBus;
        _activeContentPresenter = activeContentPresenter;

        _view.addComponent(_selectionLabel, TOP_LEFT);
        ((HorizontalLayout) _view).setExpandRatio(_selectionLabel, 1);

        _view.addComponent(_rightLabel, TOP_RIGHT);
        ((HorizontalLayout) _view).setExpandRatio(_rightLabel, 1);

        _linkToPublic.setCaption("Public");
        _linkToPublic.setVisible(false);
        _linkToPublic.setTargetName("_blank");
        _linkToPublic.addStyleName("v-label");
        _linkToPublic.setPrimaryStyleName("noprimarystylename");
        _view.addComponent(_linkToPublic, Alignment.TOP_RIGHT);

        bindHandlers();
        refresh();
        return _view;
    }

    @Override
    public void setSelectedItems(Set<Object> itemIds) {
        if (!itemIds.isEmpty()) {
            Object id = itemIds.iterator().next();
            _rootIsSelected = id.equals(_contentConnector.getDefaultItemId());
            setSelectedItem(id, itemIds.size());
        } else {
            _rootIsSelected = true;
            setSelectedItem(_contentConnector.getDefaultItemId(), itemIds.size());
        }
    }

    @Override
    public void setSelectedItem(Object itemId, int totalSelected) {
        int totalSelectedValue = totalSelected;
        // selection might contain the configured root path (by default '/') but we don't want to count that
        if (_rootIsSelected && totalSelectedValue > 0) {
            totalSelectedValue--;
        }
        if (totalSelectedValue == 1) {
            final String selectionLabel = getSelectionLabel(itemId);
            _selectionLabel.setValue(selectionLabel);
            _selectionLabel.setDescription(selectionLabel);

            if (showUuid() && itemId instanceof JcrNodeItemId) {
                LOGGER.debug("Show UUID in right side of status bar.");
                final String identifier = ((JcrNodeItemId) itemId).getUuid();
                _rightLabel.setValue(identifier);
                _rightLabel.setDescription(identifier);

                handleLinkToPublic(identifier);
            }
        } else {
            String selected = _i18n.translate("ui-contentapp.statusbar.selected", totalSelectedValue);
            _selectionLabel.setValue(selected);
            _selectionLabel.setDescription(selected);
            _linkToPublic.setVisible(false);
        }
    }

    private void handleLinkToPublic(final String identifier) {
        String path = StringUtils.EMPTY;
        final Node node = getNodeByIdentifier(identifier);
        if (node != null) {
            try {
                path = convertUUIDtoHandle(identifier, WEBSITE);
            } catch (LinkException e) {
                LOGGER.error("Error on getting path for identifier [{}]", identifier, e);
            }
        }

        if (isNotBlank(path)) {
            LOGGER.debug("Show Link to public page in right side of status bar.");
            _linkToPublic.setVisible(true);
            final String publicLink = _linkService.getPublicLink(path);
            _linkToPublic.setResource(new ExternalResource(publicLink));
        } else {
            _linkToPublic.setVisible(false);
        }
    }

    private String getSelectionLabel(Object itemId) {
        String selectionLabel = _contentConnector.getItemUrlFragment(itemId);
        if (isDamWorkspace() && _toolsModule.getStatusBarConfig() != null) {
            final List<String> assetUsageWorkspaces = _toolsModule.getStatusBarConfig().getAssetUsageWorkspaces();
            if (CollectionUtils.isNotEmpty(assetUsageWorkspaces)) {
                LOGGER.debug("Show count of referenced website content of selected node in brackets.");
                selectionLabel += " (" + getReferencesToWorkspacesCount(itemId, assetUsageWorkspaces) + ")";
            }
        }
        return selectionLabel;
    }

    private boolean isDamWorkspace() {
        return DamConstants.WORKSPACE.equals(getCurrentWorkspace());
    }

    private boolean showUuid() {
        return _toolsModule.getStatusBarConfig() != null
            && _toolsModule.getStatusBarConfig().getShowUuidWorkspaces() != null
            && _toolsModule.getStatusBarConfig().getShowUuidWorkspaces().contains(getCurrentWorkspace());
    }

    private String getCurrentWorkspace() {
        String currentWorkspace = EMPTY;
        final Object defaultItemId = _contentConnector.getDefaultItemId();
        if (defaultItemId instanceof JcrNodeItemId) {
            currentWorkspace = ((JcrNodeItemId) defaultItemId).getWorkspace();
        }
        return currentWorkspace;
    }

    protected int getReferencesToWorkspacesCount(Object itemId, List<String> assetUsageWorkspaces) {
        int size = 0;
        JcrNodeItemId jcrNodeItemId = null;
        if (itemId instanceof JcrNodeItemId) {
            jcrNodeItemId = (JcrNodeItemId) itemId;
        }
        if (jcrNodeItemId != null) {
            final String queryString = String.format("SELECT * FROM [nt:base] AS base WHERE CONTAINS(base.*, '%s')", jcrNodeItemId.getUuid());
            for (String assetUsageWorkspace : assetUsageWorkspaces) {
                size += getUsageCountInWorkspace(jcrNodeItemId, assetUsageWorkspace, queryString);
            }
        }
        return size;
    }

    private int getUsageCountInWorkspace(final JcrNodeItemId jcrNodeItemId, final String workspace, final String queryString) {
        int size = 0;

        final NodeIterator nodeIterator;
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            QueryManager manager = session.getWorkspace().getQueryManager();
            Query query = manager.createQuery(queryString, JCR_SQL2);
            QueryResult result = query.execute();
            nodeIterator = result.getNodes();
            // must convert to collection cause .getSize() returns -1
            size = NodeUtil.getCollectionFromNodeIterator(nodeIterator).size();
            LOGGER.info("Found {} references for node {} in {}.", new String[]{String.valueOf(size), jcrNodeItemId.getUuid(), workspace});
        } catch (RepositoryException e) {
            LOGGER.error("Error on getting references by node {}.", jcrNodeItemId.getUuid(), e);
        }
        return size;
    }

    @Override
    public void refresh() {
        // active presenter can be null initially when there are multiple browser sub apps
        if (_activeContentPresenter == null) {
            return;
        }

        int selected = _activeContentPresenter.getSelectedItemIds().size();
        if (selected == 1) {
            setSelectedItem(_activeContentPresenter.getSelectedItemIds().get(0), selected);
        } else {
            setSelectedItem(null, selected);
        }
    }

    @Override
    public void setActivePresenter(ContentPresenter activePresenter) {
        _activeContentPresenter = activePresenter;
    }
}