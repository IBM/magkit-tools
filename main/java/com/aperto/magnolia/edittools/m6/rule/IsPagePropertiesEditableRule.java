package com.aperto.magnolia.edittools.m6.rule;

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.pages.app.detail.PageEditorStatus;
import info.magnolia.pages.app.detail.action.availability.IsElementEditableRule;
import info.magnolia.pages.app.detail.action.availability.IsElementEditableRuleDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;

import javax.inject.Inject;
import javax.jcr.Node;

import static com.aperto.magkit.utils.NodeUtils.IS_PAGE;
import static com.aperto.magkit.utils.NodeUtils.getAncestorOrSelf;
import static com.aperto.magkit.utils.NodeUtils.getNodeByReference;
import static com.aperto.magkit.utils.NodeUtils.getTemplate;

/**
 * Generic class to check if page properties is available. We use a non-Java-Generic class to handle different elements like {@link PageElement PageElement}, {@link
 * info.magnolia.ui.vaadin.gwt.client.shared.AreaElement AreaElement}, or {@link ComponentElement ComponentElement}.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 30.06.2015
 */
public class IsPagePropertiesEditableRule extends IsElementEditableRule {

    private final PageEditorStatus _pageEditorStatus;
    private final TemplateDefinitionRegistry _templateDefinitionRegistry;

    @Inject
    public IsPagePropertiesEditableRule(final AvailabilityDefinition availabilityDefinition, final IsElementEditableRuleDefinition ruleDefinition, final PageEditorStatus pageEditorStatus,
                                        final TemplateDefinitionRegistry templateDefinitionRegistry) {
        super(availabilityDefinition, ruleDefinition, pageEditorStatus);
        _pageEditorStatus = pageEditorStatus;
        _templateDefinitionRegistry = templateDefinitionRegistry;
    }

    @Override
    protected boolean isAvailableFor(final AbstractElement element) {
        Node node = getAncestorOrSelf(getNodeByReference(RepositoryConstants.WEBSITE, _pageEditorStatus.getNodePath()), IS_PAGE);

        if (node == null) {
            return false;
        }

        DefinitionProvider<TemplateDefinition> templateDefinition = _templateDefinitionRegistry.getProvider(getTemplate(node));

        return templateDefinition.get().getDialog() != null && (templateDefinition.get().getEditable() == null || templateDefinition.get().getEditable());
    }
}
