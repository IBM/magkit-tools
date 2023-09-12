package de.ibmix.magkit.tools.t9n;

/*-
 * #%L
 * magkit-tools-t9n
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
