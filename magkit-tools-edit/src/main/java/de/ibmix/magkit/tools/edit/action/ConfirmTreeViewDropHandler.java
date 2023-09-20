package de.ibmix.magkit.tools.edit.action;

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

import de.ibmix.magkit.tools.edit.m6.ui.ConfirmRowDragger;
import de.ibmix.magkit.tools.edit.setup.EditToolsModule;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Notification;
import com.vaadin.v7.event.DataBoundTransferable;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.TreeTable;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;
import info.magnolia.ui.workbench.tree.MoveLocation;
import info.magnolia.ui.workbench.tree.drop.DropConstraint;
import info.magnolia.ui.workbench.tree.drop.TreeViewDropHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static de.ibmix.magkit.core.utils.NodeUtils.getNodeByIdentifier;
import static info.magnolia.jcr.util.NodeUtil.getPathIfPossible;

/**
 * Extended handler for drag and drop.
 *
 * @author janine.naumann, frank sommer
 * @deprecated for Magnolia 6 use {@link ConfirmRowDragger}
 */
@Deprecated
public class ConfirmTreeViewDropHandler extends TreeViewDropHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmTreeViewDropHandler.class);

    private TreeTable _tree;
    private DropConstraint _constraint;
    private SimpleTranslator _i18n;
    private Provider<EditToolsModule> _moduleProvider;

    /**
     * Constructor used for drag'n'drop.
     */
    public ConfirmTreeViewDropHandler(TreeTable tree, DropConstraint constraint, SimpleTranslator i18n, Provider<EditToolsModule> moduleProvider) {
        super(tree, constraint);
        _tree = tree;
        _constraint = constraint;
        _i18n = i18n;
        _moduleProvider = moduleProvider;
    }

    /**
     * Constructor used for move dialog.
     */
    public ConfirmTreeViewDropHandler() {
        super();
    }

    @Override
    public void drop(final DragAndDropEvent dropEvent) {
        Transferable t = dropEvent.getTransferable();
        if (t.getSourceComponent() == _tree && t instanceof DataBoundTransferable) {
            AbstractSelect.AbstractSelectTargetDetails target = (AbstractSelect.AbstractSelectTargetDetails) dropEvent.getTargetDetails();
            final Object targetItemId = target.getItemIdOver();
            final VerticalDropLocation location = target.getDropLocation();
            if (location == null) {
                LOGGER.debug("DropLocation is null. Do nothing.");
            } else {
                if (showConfirmation((JcrNodeItemId) targetItemId)) {
                    AlertBuilder.confirmDialog(getConfirmationQuestion(dropEvent))
                        .withBody(getBodyText(dropEvent))
                        .withLevel(Notification.Type.WARNING_MESSAGE)
                        .withOkButtonCaption(_i18n.translate("magkit.moveItem.confirmText"))
                        .withDeclineButtonCaption(_i18n.translate("magkit.moveItem.cancelText"))
                        .withConfirmationHandler(() -> moveItems(dropEvent, targetItemId, location))
                        .buildAndOpen();
                } else {
                    moveItems(dropEvent, targetItemId, location);
                }
            }
        }
    }

    private String getBodyText(final DragAndDropEvent dropEvent) {
        StringBuilder bodyText = new StringBuilder("<ul>");
        Collection<Object> itemIdsToMove = getItemIdsToMove(dropEvent);
        for (Object itemId : itemIdsToMove) {
            if (itemId instanceof JcrNodeItemId) {
                Node node = getNodeByIdentifier(((JcrNodeItemId) itemId).getWorkspace(), ((JcrNodeItemId) itemId).getUuid());
                bodyText.append("<li>").append(getPathIfPossible(node)).append("</li>");
            }
        }
        bodyText.append("</ul>");
        return bodyText.toString();
    }

    private boolean showConfirmation(final JcrNodeItemId targetItemId) {
        EditToolsModule editToolsModule = _moduleProvider.get();
        List<String> workspaces = editToolsModule.getMoveConfirmWorkspaces();
        return workspaces.contains(targetItemId.getWorkspace());
    }

    private void moveItems(final DragAndDropEvent dropEvent, final Object targetItemId, final VerticalDropLocation location) {
        Collection<Object> itemIdsToMove = getItemIdsToMove(dropEvent);
        moveAllNodes(itemIdsToMove, targetItemId, location);
    }

    /**
     * Iterates over all item ids to move the corresponding jcr nodes.
     * Override if you want to do sth. more.
     */
    protected void moveAllNodes(final Collection<Object> itemIdsToMove, final Object targetItemId, final VerticalDropLocation location) {
        for (Object sourceItemId : itemIdsToMove) {
            moveNode(sourceItemId, targetItemId, location);
        }
    }

    private String getConfirmationQuestion(DragAndDropEvent dropEvent) {
        int size = getItemIdsToMove(dropEvent).size();
        if (size == 1) {
            return _i18n.translate("magkit.moveItem.confirmationQuestionOneItem");
        }
        return String.format(_i18n.translate("magkit.moveItem.confirmationQuestionManyItems"), size);
    }

    /**
     * Returns a collection of itemIds to move:
     * <ul>
     * <li>all <em>selected</em> itemIds if and only if the dragging node is <em>also</em> selected</li>
     * <li>only the dragging itemId if it's not selected</li>.
     * </ul>
     *
     * @see TreeViewDropHandler#getItemIdsToMove(DragAndDropEvent)
     */
    private Collection<Object> getItemIdsToMove(DragAndDropEvent dropEvent) {
        Transferable t = dropEvent.getTransferable();
        Object draggingItemId = ((DataBoundTransferable) t).getItemId();

        // all selected itemIds if and only if the dragging node is also selected
        Set<Object> selectedItemIds = (Set<Object>) ((TreeTable) t.getSourceComponent()).getValue();
        if (selectedItemIds.contains(draggingItemId)) {
            return selectedItemIds;
        }

        // only the dragging itemId if it's not selected
        return Collections.singletonList(draggingItemId);
    }

    /**
     * Move a node within a tree onto, above or below another node depending on the drop location.
     * <p>
     * VerticalDropLocation indicating where the source node was dropped relative to the target node
     *
     * @see TreeViewDropHandler#moveNode(Node, Node, MoveLocation)
     */
    private void moveNode(Object sourceItemId, Object targetItemId, VerticalDropLocation location) {
        LOGGER.debug("DropLocation: {}", location.name());
        // Get Item from tree
        HierarchicalJcrContainer container = (HierarchicalJcrContainer) _tree.getContainerDataSource();
        JcrItemAdapter sourceItem = (JcrItemAdapter) container.getItem(sourceItemId);
        JcrItemAdapter targetItem = (JcrItemAdapter) container.getItem(targetItemId);

        // Sorting goes as
        // - If dropped ON a node, we append it as a child
        // - If dropped on the TOP part of a node, we move/add it before
        // the node
        // - If dropped on the BOTTOM part of a node, we move/add it
        // after the node

        if (location == VerticalDropLocation.MIDDLE) {
            if (_constraint.allowedAsChild(sourceItem, targetItem)) {
                // move first in the container
                moveItem(sourceItem, targetItem, MoveLocation.INSIDE);
                container.setParent(sourceItemId, targetItemId);
            }
        } else {
            Object parentId = container.getParent(targetItemId);
            // MGNLUI-4082: In case of moving a node to root whose parentId is null, get parent from target item.
            if (parentId == null && targetItem.getJcrItem() != null) {
                try {
                    parentId = JcrItemUtil.getItemId(targetItem.getJcrItem().getParent());
                } catch (RepositoryException e) {
                    LOGGER.info("Cannot execute drag and drop node " + sourceItem.getJcrItem() + " to " + targetItem.getJcrItem(), e);
                }
            }
            if (location == VerticalDropLocation.TOP) {
                if (parentId != null && _constraint.allowedBefore(sourceItem, targetItem)) {
                    // move first in the container
                    moveItem(sourceItem, targetItem, MoveLocation.BEFORE);
                    container.setParent(sourceItemId, parentId);
                }
            } else if (location == VerticalDropLocation.BOTTOM) {
                if (parentId != null && _constraint.allowedAfter(sourceItem, targetItem)) {
                    moveItem(sourceItem, targetItem, MoveLocation.AFTER);
                    container.setParent(sourceItemId, parentId);
                }
            }
        }
    }
}

