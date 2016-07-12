package com.aperto.magnolia.translation;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;

/**
 * Action for opening the translation edit dialog.
 *
 * @author diana.racho (Aperto AG)
 * @param <D> the action definition type
 */
public class OpenEditTranslationDialogAction<D extends ConfiguredActionDefinition> extends AbstractTranslationDialogAction<D> {

    private final JcrNodeAdapter _itemToEdit;

    @Inject
    public OpenEditTranslationDialogAction(D definition, JcrNodeAdapter itemToEdit, FormDialogPresenter formDialogPresenter, UiContext uiContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, I18nContentSupport i18nContentSupport) {
        super(definition, i18nContentSupport, formDialogPresenter, uiContext, eventBus);
        _itemToEdit = itemToEdit;
    }

    @Override
    public void execute() throws ActionExecutionException {
        FormDialogDefinition dialogDefinition = getDialogDefinition(WORKSPACE_TRANSLATION, PREFIX_NAME);
        startPresenter(_itemToEdit, dialogDefinition);
    }
}