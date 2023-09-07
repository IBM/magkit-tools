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
 * Base result view implementation.
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

    @Inject
    public BaseResultViewImpl(final SubAppContext subAppContext, final ComponentProvider componentProvider, final FormBuilder formBuilder, final SimpleTranslator i18n) {
        _componentProvider = componentProvider;
        _formBuilder = formBuilder;
        _i18n = i18n;
        _formDefinition = ((FormSubAppDescriptor) subAppContext.getSubAppDescriptor()).getForm();
    }

    @Override
    public Component asVaadinComponent() {
        return _content;
    }

    @Override
    public void setListener(final Listener listener) {
        _listener = listener;
        build();
    }

    @Override
    public void setFormView(final View formView) {
        _currentFormView = (FormViewReduced) formView;
        _inputSection.addComponent(formView.asVaadinComponent(), 0);
    }

    @Override
    public void refresh() {
        FormViewReduced newView = _componentProvider.getComponent(FormViewReduced.class);
        _formBuilder.buildReducedForm(_formDefinition, newView, new PropertysetItem(), null);
        _inputSection.replaceComponent(_currentFormView.asVaadinComponent(), newView.asVaadinComponent());
        _currentFormView = newView;
    }

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

    protected abstract String getButtonKey();

    @Override
    public FormViewReduced getCurrentFormView() {
        return _currentFormView;
    }

    protected CssLayout getResultSection() {
        return _resultSection;
    }

    public CssLayout getInputSection() {
        return _inputSection;
    }
}
