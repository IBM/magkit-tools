package com.aperto.magnolia.translation;

import com.google.inject.Inject;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.ui.workbench.AbstractContentPresenter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredContentPresenterDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PN_KEY;
import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;
import static java.lang.Boolean.TRUE;

/**
 * Add columns from i18n available locales.
 *
 * @author frank.sommer
 * @since 1.0.3
 */
public abstract class AbstractListPresenterDefinition extends ConfiguredContentPresenterDefinition {
    private I18nContentSupport _i18nContentSupport;

    public AbstractListPresenterDefinition(Class<? extends AbstractContentPresenter> presenterClass, String viewType) {
        setImplementationClass(presenterClass);
        setViewType(viewType);
        setActive(true);
        setIcon("icon-view-list");
    }

    @Override
    public List<ColumnDefinition> getColumns() {
        List<ColumnDefinition> newColumns = new ArrayList<>();
        for (Locale locale : _i18nContentSupport.getLocales()) {
            PropertyColumnDefinition column = new PropertyColumnDefinition();
            column.setName(PREFIX_NAME + locale.getLanguage());
            column.setPropertyName(PREFIX_NAME + locale.getLanguage());
            column.setEditable(TRUE);
            column.setSortable(TRUE);
            column.setExpandRatio(2);
            column.setLabel(locale.getDisplayName());
            newColumns.add(column);
        }

        insertConfiguredColumns(newColumns);
        return newColumns;
    }

    private void insertConfiguredColumns(final List<ColumnDefinition> newColumns) {
        List<ColumnDefinition> oldColumns = super.getColumns();
        for (ColumnDefinition column : oldColumns) {
            String columnName = column.getName();
            if (!columnName.startsWith(PREFIX_NAME)) {
                if (PN_KEY.equals(columnName)) {
                    newColumns.add(0, column);
                } else {
                    newColumns.add(column);
                }
            }
        }
    }

    @Inject
    public void setI18nContentSupport(I18nContentSupport i18nContentSupport) {
        _i18nContentSupport = i18nContentSupport;
    }
}