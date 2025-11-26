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

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.v7.data.util.PropertysetItem;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.formdialog.FormBuilder;
import info.magnolia.ui.form.definition.FormDefinition;
import info.magnolia.ui.vaadin.form.FormViewReduced;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;

import javax.inject.Inject;

/**
 * Abstract base implementation for result views in tool sub-applications.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Provides common layout structure with input section and result section</li>
 *   <li>Manages form view lifecycle and refresh operations</li>
 *   <li>Creates action buttons with i18n support</li>
 *   <li>Implements listener pattern for action events</li>
 * </ul>
 * <p><strong>Layout Structure:</strong></p>
 * The view is divided into two main sections:
 * <ul>
 *   <li>Input section: contains the form and action button</li>
 *   <li>Result section: displays operation results (managed by subclasses)</li>
 * </ul>
 *
 * @author frank.sommer
 * @see VersionPruneResultView
 * @since 1.5.0
 */
public abstract class BaseResultViewImpl implements ResultView {
    private final ComponentProvider _componentProvider;
    private final FormBuilder _formBuilder;
    private final SimpleTranslator _i18n;
    private final FormDefinition _formDefinition;

    private final SmallAppLayout _content = new SmallAppLayout();
    private final CssLayout _inputSection = new CssLayout();
    private final CssLayout _buttonLayout = new CssLayout();
    private final CssLayout _resultSection = new CssLayout();

    private Listener _listener;
    private FormViewReduced _currentFormView;

    /**
     * Constructs a new BaseResultViewImpl instance.
     *
     * @param subAppContext the sub-application context
     * @param componentProvider the component provider for creating new instances
     * @param formBuilder the form builder for constructing forms
     * @param i18n the translator for i18n support
     */
    @Inject
    public BaseResultViewImpl(final SubAppContext subAppContext, final ComponentProvider componentProvider, final FormBuilder formBuilder, final SimpleTranslator i18n) {
        _componentProvider = componentProvider;
        _formBuilder = formBuilder;
        _i18n = i18n;
        _formDefinition = ((FormSubAppDescriptor) subAppContext.getSubAppDescriptor()).getForm();
    }

    /**
     * Returns the Vaadin component representation of this view.
     *
     * @return the root Vaadin component
     */
    @Override
    public Component asVaadinComponent() {
        return _content;
    }

    /**
     * Sets the listener for action events and builds the view.
     *
     * @param listener the listener to handle action triggers
     */
    @Override
    public void setListener(final Listener listener) {
        _listener = listener;
        build();
    }

    /**
     * Sets the form view and adds it to the input section.
     *
     * @param formView the form view to display
     */
    @Override
    public void setFormView(final View formView) {
        _currentFormView = (FormViewReduced) formView;
        _inputSection.addComponent(formView.asVaadinComponent(), 0);
    }

    /**
     * Refreshes the form view by creating a new form instance and replacing the old one.
     */
    @Override
    public void refresh() {
        FormViewReduced newView = _componentProvider.getComponent(FormViewReduced.class);
        _formBuilder.buildReducedForm(_formDefinition, newView, new PropertysetItem(), null);
        _inputSection.replaceComponent(_currentFormView.asVaadinComponent(), newView.asVaadinComponent());
        _currentFormView = newView;
    }

    /**
     * Builds the view structure including action button and layout sections.
     */
    private void build() {
        Button executeButton = new Button(_i18n.translate(getButtonKey()));
        executeButton.addStyleName("v-button-smallapp");
        executeButton.addStyleName("commit");
        executeButton.addClickListener((ClickListener) event -> _listener.onActionTriggered());
        _buttonLayout.addStyleName("v-csslayout-smallapp-actions");
        _buttonLayout.addComponent(executeButton);
        _inputSection.addComponent(_buttonLayout);
        _content.addSection(_inputSection);
        _content.addSection(_resultSection);
    }

    /**
     * Returns the i18n key for the action button label.
     * Must be implemented by subclasses to provide appropriate button text.
     *
     * @return the i18n key for the button
     */
    protected abstract String getButtonKey();

    /**
     * Returns the current form view instance.
     *
     * @return the current form view
     */
    @Override
    public FormViewReduced getCurrentFormView() {
        return _currentFormView;
    }

    /**
     * Returns the result section layout for displaying operation results.
     *
     * @return the result section layout
     */
    protected CssLayout getResultSection() {
        return _resultSection;
    }

    /**
     * Returns the input section layout containing the form and action button.
     *
     * @return the input section layout
     */
    public CssLayout getInputSection() {
        return _inputSection;
    }
}
