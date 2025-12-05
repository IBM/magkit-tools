package de.ibmix.magkit.tools.t9n;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Translation
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.ViewType;
import info.magnolia.ui.contentapp.configuration.ListViewDefinition;
import info.magnolia.ui.contentapp.configuration.column.ColumnDefinition;
import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;
import lombok.Generated;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <p><strong>Purpose:</strong> A custom list view definition for displaying translation data in the Magnolia
 * UI content application.</p>
 *
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 * <li>Extends the standard {@link ListViewDefinition} to provide translation-specific column configuration</li>
 * <li>Automatically generates translation columns based on configured locales from {@link I18nContentSupport}</li>
 * <li>Dynamically displays language columns with localized language names based on the current user's language</li>
 * <li>Combines translation-specific columns with standard columns from the parent definition</li>
 * </ul>
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Translation column names follow the naming convention: <code>PREFIX_NAME + locale</code></li>
 * <li>Column headers display localized language names (e.g., "English", "Deutsch")</li>
 * <li>All translation columns are sortable with a fixed width of 300 pixels</li>
 * <li>Includes a "key" column for identifying translation entries</li>
 * </ul>
 *
 * <p><strong>Usage Preconditions:</strong></p>
 * <ul>
 * <li>Requires {@link I18nContentSupport} component to be available in the Magnolia context</li>
 * <li>Requires the current user to have a language preference configured in their profile</li>
 * <li>Must be registered with the {@code @ViewType("translationListView")} annotation</li>
 * </ul>
 *
 * <p><strong>Thread-Safety:</strong> This class is thread-safe as it does not maintain mutable state between
 * method calls. However, the {@link I18nContentSupport} and {@link MgnlContext} components should be
 * thread-safe.</p>
 *
 * @param <T> the type of items displayed in the list view
 *
 * @author wolf.bubenik IBM iX
 * @since 2025-12-05
 */
@ViewType("translationListView")
public class TranslationListViewDefinition<T> extends ListViewDefinition<T> {

    /**
     * Retrieves the complete list of column definitions, combining translation-specific columns
     * with columns from the parent list view definition.
     *
     * @return a list of {@link ColumnDefinition} objects, with translation columns added first
     */
    @Override
    @Generated
    public List<ColumnDefinition<T>> getColumns() {
        List<ColumnDefinition<T>> columns = getTranslationColumns();
        columns.addAll(super.getColumns());
        return columns;
    }

    /**
     * Generates translation column definitions based on configured locales and the user's language preference.
     *
     * <p>This method creates columns for:</p>
     * <ul>
     * <li>A "key" column for identifying translation entries</li>
     * <li>One column per locale configured in {@link I18nContentSupport}, with localized language names as headers</li>
     * </ul>
     *
     * @return a list of {@link ColumnDefinition} objects for all translation columns
     */
    private List<ColumnDefinition<T>> getTranslationColumns() {
        List<ColumnDefinition<T>> columns = new ArrayList<>();
        columns.add(buildTranslationColumn("key", null));
        I18nContentSupport i18nContentSupport = Components.getComponent(I18nContentSupport.class);
        final Locale userLocale = new Locale(MgnlContext.getUser().getLanguage());
        for (Locale locale : i18nContentSupport.getLocales()) {
            String name = TranslationNodeTypes.Translation.PREFIX_NAME + locale;
            String label = StringUtils.capitalize(locale.getDisplayLanguage(userLocale));
            columns.add(buildTranslationColumn(name, label));
        }
        return columns;
    }

    /**
     * Constructs a configured column definition for a translation column.
     *
     * @param name the name of the column, used as property name for data binding
     * @param label the display label for the column header; may be {@code null} for the "key" column
     *
     * @return a configured {@link ColumnDefinition} with sortable enabled and a fixed width of 300 pixels
     */
    public ColumnDefinition<T> buildTranslationColumn(String name, String label) {
        ConfiguredColumnDefinition<T> column = new ConfiguredColumnDefinition<T>();
        column.setName(name);
        column.setPropertyName(name);
        column.setLabel(label);
        column.setSortable(true);
        column.setWidth(300);
        return column;
    }
}
