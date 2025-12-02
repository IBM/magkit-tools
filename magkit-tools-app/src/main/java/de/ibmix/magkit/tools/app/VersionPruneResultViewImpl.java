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

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.dialog.formdialog.FormBuilder;

import javax.inject.Inject;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Implementation of VersionPruneResultView for displaying version pruning results.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Displays pruning results in a read-only text area</li>
 *   <li>Shows description and usage hints in the input section</li>
 *   <li>Updates result display after each pruning operation</li>
 * </ul>
 *
 * @author frank.sommer
 * @see VersionPruneResultView
 * @since 1.5.0
 */
public class VersionPruneResultViewImpl extends BaseResultViewImpl implements VersionPruneResultView {

    private final TextArea _textArea;

    /**
     * Constructs a new VersionPruneResultViewImpl instance and initializes the result text area.
     *
     * @param subAppContext the sub-application context
     * @param componentProvider the component provider
     * @param formBuilder the form builder
     * @param i18n the translator for i18n support
     */
    @Inject
    public VersionPruneResultViewImpl(final SubAppContext subAppContext, final ComponentProvider componentProvider, final FormBuilder formBuilder, final SimpleTranslator i18n) {
        super(subAppContext, componentProvider, formBuilder, i18n);
        CssLayout inputSection = getInputSection();
        Label description = new Label(i18n.translate("versionPrune.description"));
        description.addStyleName("prune-hint");
        inputSection.addComponentAsFirst(description);

        CssLayout resultSection = getResultSection();
        _textArea = new TextArea();
        _textArea.setRows(20);
        _textArea.setWidth("100%");
        resultSection.addComponent(_textArea);
    }

    /**
     * Returns the i18n key for the prune action button.
     *
     * @return the i18n key "versionPrune.submitPrune"
     */
    @Override
    protected String getButtonKey() {
        return "versionPrune.submitPrune";
    }

    /**
     * Displays the pruning results in the read-only text area.
     *
     * @param result the text result containing pruning information
     */
    @Override
    public void buildResultView(String result) {
        // set first read only false, otherwise the new value can't set
        _textArea.setReadOnly(false);
        _textArea.setValue(defaultString(result));
        _textArea.setReadOnly(true);
    }
}
