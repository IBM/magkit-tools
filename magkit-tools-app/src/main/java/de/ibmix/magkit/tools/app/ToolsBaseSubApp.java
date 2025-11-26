package de.ibmix.magkit.tools.app;

/*-
 * #%L
 * magkit-tools-app
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

import com.vaadin.v7.data.util.PropertysetItem;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.framework.app.BaseSubApp;
import info.magnolia.ui.vaadin.form.FormViewReduced;

/**
 * Abstract base class for form-based tool sub-applications.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Manages form view and result view lifecycle</li>
 *   <li>Handles form validation before action execution</li>
 *   <li>Provides template method pattern for action execution</li>
 *   <li>Integrates form builder with sub-app context</li>
 * </ul>
 * <p><strong>Usage:</strong></p>
 * Subclasses must implement the {@link #doAction()} method to define the specific
 * action to be performed when the form is valid and submitted.
 *
 * @param <T> the type of result view extending ResultView
 * @author frank.sommer
 * @since 1.5.0
 */
public abstract class ToolsBaseSubApp<T extends ResultView> extends BaseSubApp<ResultView> implements ResultView.Listener {
    private final T _view;
    private final FormBuilder _builder;
    private final FormViewReduced _formView;
    private final FormDefinition _formDefinition;

    /**
     * Constructs a new ToolsBaseSubApp instance.
     *
     * @param subAppContext the sub-application context
     * @param formView the reduced form view for input
     * @param view the result view for displaying output
     * @param builder the form builder for constructing forms
     */
    public ToolsBaseSubApp(final SubAppContext subAppContext, final FormViewReduced formView, final T view, final FormBuilder builder) {
        super(subAppContext, view);
        _formView = formView;
        _view = view;
        _builder = builder;
        _formDefinition = ((FormSubAppDescriptor) subAppContext.getSubAppDescriptor()).getForm();
    }

    /**
     * Handles action trigger events from the view.
     * Validates the form before executing the action.
     */
    public void onActionTriggered() {
        _formView.showValidation(true);
        if (_formView.isValid()) {
            doAction();
        }
    }

    /**
     * Executes the specific action for this sub-app.
     * Must be implemented by subclasses to define the actual operation.
     */
    protected abstract void doAction();

    /**
     * Initializes the sub-app by building the form and connecting it to the view.
     */
    @Override
    protected void onSubAppStart() {
        PropertysetItem item = new PropertysetItem();

        _builder.buildReducedForm(_formDefinition, _formView, item, null);
        _view.setFormView(_formView);
        _view.setListener(this);
    }

    /**
     * Returns the form definition for this sub-app.
     *
     * @return the form definition
     */
    protected FormDefinition getFormDefinition() {
        return _formDefinition;
    }
}
