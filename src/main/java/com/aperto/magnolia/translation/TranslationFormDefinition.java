package com.aperto.magnolia.translation;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
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
 * Wraps the configured form to add additional fields.
 *
 * @author frank.sommer
 * @since 01.10.2020
 */
public class TranslationFormDefinition extends ConfiguredFormDefinition {

    @Override
    public List<EditorPropertyDefinition> getProperties() {
        List<EditorPropertyDefinition> properties = new ArrayList<>(super.getProperties());
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
