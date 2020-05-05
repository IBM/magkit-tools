package com.aperto.magnolia.translation;

import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.EditorValidator;
import info.magnolia.ui.form.action.SaveFormAction;
import info.magnolia.ui.form.action.SaveFormActionDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Custom save action used in app definition.
 *
 * @author frank.sommer
 * @since 1.4.1
 */
public class TranslationSaveFormAction extends SaveFormAction {
    private final NodeNameHelper _nodeNameHelper;

    @Inject
    public TranslationSaveFormAction(SaveFormActionDefinition definition, JcrNodeAdapter item, EditorCallback callback, EditorValidator validator, NodeNameHelper nodeNameHelper) {
        super(definition, item, callback, validator);
        _nodeNameHelper = nodeNameHelper;
    }

    /**
     * Save the new node under the value of the key property.
     * It's a copy of the super class.
     */
    @Override
    protected void setNodeName(Node node, JcrNodeAdapter item) throws RepositoryException {
        String propertyName = TranslationNodeTypes.Translation.PN_KEY;
        if (node.hasProperty(propertyName) && !node.hasProperty("jcrName")) {
            Property property = node.getProperty(propertyName);
            String newNodeName = property.getString();
            String validatedName = _nodeNameHelper.getValidatedName(newNodeName);
            if (!node.getName().equals(validatedName)) {
                newNodeName = _nodeNameHelper.getUniqueName(node.getSession(), node.getParent().getPath(), validatedName);
                item.setNodeName(newNodeName);
                NodeUtil.renameNode(node, newNodeName);
            }
        }
    }
}