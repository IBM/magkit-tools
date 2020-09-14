package com.aperto.magnolia.translation;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.dialog.DialogDefinition;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.dialog.definition.ConfiguredFormDialogDefinition;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.EditorCallback;
import info.magnolia.ui.form.definition.TabDefinition;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.TextFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.apache.commons.lang.StringUtils;

import javax.inject.Named;
import java.util.List;
import java.util.Locale;

/**
 * Abstract base class for actions that open dialogs for adding or editing locale properties.
 *
 * @param <D> the action definition type
 * @author diana.racho (Aperto AG)
 */
public abstract class AbstractTranslationDialogAction<D extends ActionDefinition> extends AbstractAction<D> {
    private static final String TRANSLATION_DIALOG_NAME = "magnolia-translation:editTranslation";

    private final I18nContentSupport _i18nContentSupport;
    private final FormDialogPresenter _formDialogPresenter;
    private final UiContext _uiContext;
    private final EventBus _eventBus;

    protected AbstractTranslationDialogAction(D definition, I18nContentSupport i18nContentSupport, FormDialogPresenter formDialogPresenter, UiContext uiContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus) {
        super(definition);
        _i18nContentSupport = i18nContentSupport;
        _formDialogPresenter = formDialogPresenter;
        _uiContext = uiContext;
        _eventBus = eventBus;
    }

    protected FormDialogDefinition getDialogDefinition() throws ActionExecutionException {
        DialogDefinitionRegistry dialogDefinitionRegistry = Components.getComponent(DialogDefinitionRegistry.class);
        DefinitionProvider<DialogDefinition> provider = dialogDefinitionRegistry.getProvider(TRANSLATION_DIALOG_NAME);
        ConfiguredFormDialogDefinition dialogDefinition = (ConfiguredFormDialogDefinition) provider.get();

        if (dialogDefinition == null) {
            throw new ActionExecutionException("Unable to load dialog [" + TRANSLATION_DIALOG_NAME + "]");
        }

        List<TabDefinition> tabs = dialogDefinition.getForm().getTabs();
        for (TabDefinition tab : tabs) {
            if ("main".equals(tab.getName())) {
                addFieldsForLocales(tab);
            }
        }

        return dialogDefinition;
    }

    private void addFieldsForLocales(final TabDefinition tab) {
        for (Locale locale : _i18nContentSupport.getLocales()) {
            String displayName = TranslationNodeTypes.Translation.PREFIX_NAME + locale.getLanguage();
            boolean hasFieldForDisplayName = hasField(tab, displayName);

            if (!hasFieldForDisplayName) {
                TextFieldDefinition field = new TextFieldDefinition();
                field.setName(displayName);
                field.setLabel(StringUtils.capitalize(locale.getDisplayLanguage()));
                tab.getFields().add(field);
            }
        }
    }

    private boolean hasField(TabDefinition tab, String name) {
        for (FieldDefinition fieldDefinition : tab.getFields()) {
            if (fieldDefinition.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    protected void startPresenter(final JcrNodeAdapter item, FormDialogDefinition dialogDefinition) {
        _formDialogPresenter.start(item, dialogDefinition, _uiContext, new EditorCallback() {

            @Override
            public void onSuccess(String actionName) {
                _eventBus.fireEvent(new ContentChangedEvent(item.getItemId(), true));
                _formDialogPresenter.closeDialog();
            }

            @Override
            public void onCancel() {
                _formDialogPresenter.closeDialog();
            }
        });
    }
}