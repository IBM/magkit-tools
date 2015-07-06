package com.aperto.magnolia.edittools.action;

import info.magnolia.cms.core.Path;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.framework.action.AbstractRepositoryAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * This action checks the user http session for a node reference (JcrItemAdapter), which is then added/duplicated as new node within the user selected area.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 06.07.2015
 */
public class PasteNodeAction extends AbstractRepositoryAction<PasteNodeActionDefinition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasteNodeAction.class);

    @Inject
    public PasteNodeAction(PasteNodeActionDefinition definition, JcrItemAdapter item, @Named(SubAppEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, item, eventBus);
    }

    @Override
    protected void onExecute(final JcrItemAdapter item) throws RepositoryException {
        Object copyNode = MgnlContext.getAttribute(CopyNodeAction.class.getName(), Context.SESSION_SCOPE);
        if (copyNode instanceof JcrItemAdapter && item != null && item.getJcrItem().isNode()) {
            Item copyItem = ((JcrItemAdapter) copyNode).getJcrItem();
            Item parentItem = item.getJcrItem();
            if (copyItem.isNode() && parentItem.isNode()) {
                Node node = (Node) copyItem;
                Node parentNode = (Node) parentItem;

                // Generate name and path of the new node
                String newName = getUniqueNewItemName(parentNode, node.getName());
                String newPath = Path.getAbsolutePath(parentNode.getPath(), newName);

                // Duplicate node
                node.getSession().getWorkspace().copy(node.getPath(), newPath);

                // Update metadata
                Node duplicateNode = node.getSession().getNode(newPath);

                activatableUpdate(duplicateNode, MgnlContext.getUser().getName());
                // Set item of the new node for the ContentChangedEvent
                JcrItemId itemId = JcrItemUtil.getItemId(duplicateNode);
                setItemIdOfChangedItem(itemId);
            }
        } else {
            LOGGER.info("Nothing to paste from");
        }
    }


    protected void activatableUpdate(Node node, String userName) throws RepositoryException {
        if (NodeUtil.isNodeType(node, NodeTypes.Activatable.NAME) || (RepositoryConstants.CONFIG.equals(node.getSession().getWorkspace().getName()) && NodeUtil.isNodeType(node, NodeTypes.ContentNode.NAME))) {
            NodeTypes.Activatable.update(node, userName, false);
        }

        NodeIterator nodeIterator = node.getNodes();
        while (nodeIterator.hasNext()) {
            activatableUpdate(nodeIterator.nextNode(), userName);
        }
    }
}
