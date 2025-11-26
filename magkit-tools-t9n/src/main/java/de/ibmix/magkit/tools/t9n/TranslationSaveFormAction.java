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
 * Custom form save action that ensures translation nodes are named according to their key property.
 * <p><strong>Purpose:</strong></p>
 * This action extends the standard commit behavior to rename newly created translation nodes based on
 * their key property value, ensuring consistent and meaningful node names in the translation workspace.
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Automatically renames new nodes based on the translation key</li>
 * <li>Generates valid and unique JCR node names</li>
 * <li>Maintains data integrity through proper commit handling</li>
 * <li>Triggers datasource observation for UI updates</li>
 * </ul>
 * <p><strong>Usage:</strong></p>
 * This action is typically configured in the translation app definition and is automatically
 * invoked when saving translation entries through the Magnolia UI.
 *
 * @author frank.sommer
 * @since 2020-10-01
 */
public class TranslationSaveFormAction extends CommitAction<Node> {
    private final NodeNameHelper _nodeNameHelper;

    /**
     * Creates a new translation save form action with all required dependencies.
     *
     * @param definition the action definition configuration
     * @param closeHandler the handler for closing the form
     * @param valueContext the context providing access to the edited node
     * @param form the form view containing the edited data
     * @param datasource the datasource managing the JCR nodes
     * @param datasourceObservation the observation mechanism for triggering UI updates
     * @param nodeNameHelper the helper for generating valid JCR node names
     */
    @Inject
    public TranslationSaveFormAction(CommitActionDefinition definition, CloseHandler closeHandler, ValueContext<Node> valueContext, FormView<Node> form, Datasource<Node> datasource, DatasourceObservation.Manual datasourceObservation, NodeNameHelper nodeNameHelper) {
        super(definition, closeHandler, valueContext, form, datasource, datasourceObservation);
        _nodeNameHelper = nodeNameHelper;
    }

    /**
     * Writes the form data to the node and renames new nodes based on their key property value.
     * For new nodes, the node name is derived from the translation key to ensure meaningful and
     * unique identifiers in the JCR repository.
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
