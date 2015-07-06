package com.aperto.magnolia.edittools.action;

import com.vaadin.data.Property;
import info.magnolia.context.Context;
import info.magnolia.context.WebContext;
import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.LocaleProvider;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.i18nsystem.TranslationService;
import info.magnolia.jcr.util.NodeTypes.Renderable;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.framework.action.AbstractRepositoryAction;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.overlay.MessageStyleTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

/**
 * Copy action allows to select a node and store the reference in the users session. The opposite {@link PasteNodeAction} looks in the session and creates a new node in user selected area.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 04.07.2015
 */
public class CopyNodeAction extends AbstractRepositoryAction<CopyNodeActionDefinition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CopyNodeAction.class);

    private final SimpleTranslator _simpleTranslator;
    private final WebContext _webContext;
    private final UiContext _uiContext;
    private TranslationService _translationService;
    private LocaleProvider _localeProvider;
    private TemplateDefinitionRegistry _templateRegistry;

    @Inject
    public CopyNodeAction(CopyNodeActionDefinition definition, JcrItemAdapter item, @Named(AdmincentralEventBus.NAME) EventBus eventBus, UiContext uiContext, SimpleTranslator simpleTranslator, WebContext webContext) {
        super(definition, item, eventBus);
        _simpleTranslator = simpleTranslator;
        _webContext = webContext;
        _uiContext = uiContext;
    }

    @Override
    protected void onExecute(final JcrItemAdapter item) throws RepositoryException {
        if (item != null && item.getJcrItem().isNode()) {
            _webContext.setAttribute(CopyNodeAction.class.getName(), item, Context.SESSION_SCOPE);
            _uiContext.openNotification(MessageStyleTypeEnum.INFO, true, _simpleTranslator.translate("copy.message.text", getComponentName(item)));
        }
    }

    protected String getComponentName(final JcrItemAdapter node) {
        String name = StringUtils.EMPTY;

        try {
            Property templateId = node.getItemProperty(Renderable.TEMPLATE);
            if (templateId != null) {
                TemplateDefinition definition = _templateRegistry.getTemplateDefinition(String.valueOf(templateId.getValue()));
                name = _translationService.translate(_localeProvider, definition.getI18nBasename(), new String[]{definition.getTitle()});
            }
        } catch (RegistrationException e) {
            LOGGER.debug("Unable to get template title for node [{}] in [{}]", new Object[]{node.getItemId(), node.getWorkspace(), e});
        }

        return name;
    }

    @Inject
    public void setTemplateRegistry(final TemplateDefinitionRegistry templateRegistry) {
        _templateRegistry = templateRegistry;
    }

    @Inject
    public void setLocaleProvider(LocaleProvider localeProvider) {
        _localeProvider = localeProvider;
    }

    @Inject
    public void setTranslationService(TranslationService translationService) {
        _translationService = translationService;
    }
}
