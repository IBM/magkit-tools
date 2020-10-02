package com.aperto.magnolia.translation;

import com.machinezoo.noexception.Exceptions;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.contentapp.action.CommitAction;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.observation.DatasourceObservation;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;

/**
 * Custom save action used in app definition.
 *
 * @author frank.sommer
 * @since 1.4.1
 */
public class TranslationSaveFormAction extends CommitAction<Node> {
    private final NodeNameHelper _nodeNameHelper;

    @Inject
    public TranslationSaveFormAction(CommitActionDefinition definition, CloseHandler closeHandler, ValueContext<Node> valueContext, FormView<Node> form, Datasource<Node> datasource, DatasourceObservation.Manual datasourceObservation, NodeNameHelper nodeNameHelper) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation);
        _nodeNameHelper = nodeNameHelper;
    }

    /**
     * Save the new node under the value of the key property.
     */
    @Override
    protected void write() {
        getValueContext().getSingle().ifPresent(Exceptions.wrap().consumer(item -> {
            getForm().write(item);

            String propertyName = TranslationNodeTypes.Translation.PN_KEY;
            if (item.isNew() && item.hasProperty(propertyName)) {
                Property property = item.getProperty(propertyName);
                String newNodeName = _nodeNameHelper.getUniqueName(item.getParent(), _nodeNameHelper.getValidatedName(property.getString()));
                NodeUtil.renameNode(item, newNodeName);
            }

            getDatasource().commit(item);
            getDatasourceObservation().trigger();
        }));
    }
}