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

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.editor.ConfiguredFormDefinition;
import info.magnolia.ui.field.EditorPropertyDefinition;
import info.magnolia.ui.field.TextFieldDefinition;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Dynamic form definition that generates translation fields based on configured locales.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * Automatically creates form fields for each configured locale in the system, eliminating the need
 * for static form field definitions and enabling flexible multi-language translation management.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Dynamically generates form fields for all configured locales</li>
 * <li>Creates a key field for the translation identifier</li>
 * <li>Generates locale-specific fields with user-friendly labels</li>
 * <li>Supports both language-only and language-country locale combinations</li>
 * <li>Displays locale names in the current user's language</li>
 * </ul>
 * <p>
 * <p><strong>Usage:</strong></p>
 * This form definition is typically referenced in the translation app configuration and
 * automatically provides the appropriate fields based on the system's i18n configuration.
 *
 * @author frank.sommer
 * @since 2020-10-01
 */
public class TranslationFormDefinition extends ConfiguredFormDefinition {

    /**
     * Generates the form property definitions including a key field and one text field per configured locale.
     * Field labels are localized to the current user's language.
     *
     * @return the list of form field definitions
     */
    @Override
    public List<EditorPropertyDefinition> getProperties() {
        List<EditorPropertyDefinition> properties = new ArrayList<>();

        final TextFieldDefinition keyProperty = new TextFieldDefinition();
        keyProperty.setName(TranslationNodeTypes.Translation.PN_KEY);
        final SimpleTranslator simpleTranslator = Components.getComponent(SimpleTranslator.class);
        keyProperty.setLabel(simpleTranslator.translate("translation.jcrDetail.main.key.label"));
        properties.add(keyProperty);

        I18nContentSupport i18nContentSupport = Components.getComponent(I18nContentSupport.class);
        final Locale userLocale = new Locale(MgnlContext.getUser().getLanguage());
        for (Locale locale : i18nContentSupport.getLocales()) {
            String displayName = TranslationNodeTypes.Translation.PREFIX_NAME + locale.toString();
            TextFieldDefinition field = new TextFieldDefinition();
            field.setName(displayName);
            String label = StringUtils.capitalize(locale.getDisplayLanguage(userLocale));
            if (isNotEmpty(locale.getCountry())) {
                label += " (" + locale.getDisplayCountry(userLocale) + ")";
            }
            field.setLabel(label);
            properties.add(field);
        }
        return properties;
    }
}
