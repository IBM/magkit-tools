package com.aperto.magnolia.edittools.rule;

import com.aperto.magkit.utils.NodeUtils;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.pages.app.editor.PageEditorPresenter;
import info.magnolia.pages.app.editor.availability.AbstractElementAvailabilityRule;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import info.magnolia.ui.vaadin.gwt.client.shared.PageElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Generic class to check if page properties is available. We use a non-Java-Generic class to handle different elements like {@link info.magnolia.ui.vaadin.gwt.client.shared.PageElement PageElement}, {@link
 * info.magnolia.ui.vaadin.gwt.client.shared.AreaElement AreaElement}, or {@link info.magnolia.ui.vaadin.gwt.client.shared.ComponentElement ComponentElement}.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 30.06.2015
 */
public class IsElementEditableRule extends AbstractElementAvailabilityRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsElementEditableRule.class);

    private TemplateDefinitionRegistry _templateDefinitionRegistry;

    public IsElementEditableRule(final PageEditorPresenter pageEditorPresenter) {
        super(pageEditorPresenter, AbstractElement.class);
    }

    @Override
    protected boolean isAvailableForElement(final AbstractElement element) {
        boolean result = false;
        if (element instanceof PageElement) {
            result = element.getDialog() != null;
        } else if (element != null && isNotBlank(element.getPath()) && isNotBlank(element.getWorkspace())) {
            try {
                Node pageNode = SessionUtil.getNode(element.getWorkspace(), element.getPath());
                //Node pageNode = SessionUtil.getNode(element.getWorkspace(), element.getPath());
                while (pageNode != null && pageNode.getDepth() > 1 && !NodeUtils.isNodeType(pageNode, NodeTypes.Page.NAME)) {
                    pageNode = pageNode.getParent();
                }
                if (NodeUtils.isNodeType(pageNode, NodeTypes.Page.NAME)) {
                    TemplateDefinition templateDefinition = _templateDefinitionRegistry.getTemplateDefinition(NodeUtils.getTemplate(pageNode));
                    result = templateDefinition.getDialog() != null && (templateDefinition.getEditable() == null || templateDefinition.getEditable());
                }
            } catch (RepositoryException | RegistrationException e) {
                LOGGER.debug("Unable to check page template for dialog");
            }
        }

        return result;
    }

    @Inject
    public void setTemplateDefinitionRegistry(final TemplateDefinitionRegistry templateDefinitionRegistry) {
        _templateDefinitionRegistry = templateDefinitionRegistry;
    }
}
