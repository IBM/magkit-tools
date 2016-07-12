package com.aperto.magnolia.translation;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.vaadin.integration.jcr.AbstractJcrNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;

/**
 * Action for opening the add translation dialog.
 *
 * @author diana.racho (Apertp AG)
 * @param <D> the action definition type
 */
public class OpenAddTranslationDialogAction<D extends ConfiguredActionDefinition> extends AbstractTranslationDialogAction<D> {

    private final AbstractJcrNodeAdapter _parentItem;

    @Inject
    public OpenAddTranslationDialogAction(D definition, JcrNodeAdapter parentItem, FormDialogPresenter formDialogPresenter, UiContext uiContext, @Named(AdmincentralEventBus.NAME) final EventBus eventBus, I18nContentSupport i18nContentSupport) {
        super(definition, i18nContentSupport, formDialogPresenter, uiContext, eventBus);
        _parentItem = parentItem;
    }

    @Override
    public void execute() throws ActionExecutionException {
        FormDialogDefinition dialogDefinition = getDialogDefinition(WORKSPACE_TRANSLATION, PREFIX_NAME);
        Node parentNode = _parentItem.getJcrItem();
        final JcrNodeAdapter item = new JcrNewNodeAdapter(parentNode, TranslationNodeTypes.Translation.NAME);
        startPresenter(item, dialogDefinition);
    }
}