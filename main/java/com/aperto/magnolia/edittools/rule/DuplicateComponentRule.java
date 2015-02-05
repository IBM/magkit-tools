package com.aperto.magnolia.edittools.rule;

import com.aperto.magnolia.edittools.util.TemplateDefinitionTraverser;
import com.aperto.magnolia.edittools.util.TemplateNamePredicate;
import info.magnolia.module.templatingkit.sites.Site;
import info.magnolia.module.templatingkit.templates.pages.STKPage;
import info.magnolia.multisite.sites.MultiSiteManager;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.availability.AbstractAvailabilityRule;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import org.apache.commons.collections15.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Iterator;

import static com.aperto.magkit.utils.NodeUtils.getNodeByIdentifier;
import static com.aperto.magkit.utils.NodeUtils.getTemplate;
import static info.magnolia.jcr.util.NodeTypes.*;
import static info.magnolia.jcr.util.NodeUtil.*;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.math.NumberUtils.toInt;

/**
 * Checkt, ob weitere Elemente einer Area hinzugefügt werden können.
 * Es wird in der TemplateDefinition und im Multisite Prototype nach dem Wert für 'maxComponents' gesucht.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 18.11.14
 */
public class DuplicateComponentRule extends AbstractAvailabilityRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateComponentRule.class);

    private static final TemplateDefinitionTraverser TRAVERSER = new TemplateDefinitionTraverser();
    private static final String PARAM_MAX_COMPONENTS = "maxComponents";

    private TemplateDefinitionRegistry _templateDefinitionRegistry;

    private MultiSiteManager _multiSiteManager;

    @Override
    protected boolean isAvailableForItem(final Object itemId) {

        boolean isAvailable = false;

        if (itemId instanceof JcrNodeItemId) {
            JcrNodeItemId nodeId = (JcrNodeItemId) itemId;
            Node node = getNodeByIdentifier(nodeId.getWorkspace(), nodeId.getUuid());

            if (node != null) {
                try {
                    Node areaNode = getNearestAncestorOfType(node, Area.NAME);
                    Node pageNode = getNearestAncestorOfType(node, Page.NAME);

                    if (areaNode != null && isNotBlank(getTemplate(pageNode))) {
                        int maxComponents = getMaxComponents(node, areaNode, pageNode);
                        isAvailable = asList(getNodes(areaNode, Component.NAME)).size() < maxComponents;
                    }

                } catch (RepositoryException e) {
                    LOGGER.debug("Unable to get parent area from node [{}]", getPathIfPossible(node), e);
                }
            }
        }
        return isAvailable;
    }

    private int getMaxComponents(Node node, Node areaNode, Node pageNode) throws RepositoryException {
        int maxComponents;
        String areaName = areaNode.getName();
        int templateMaxComponents = getNumericComponents(getTemplateAreaDefinition(areaName, getTemplate(pageNode)));
        int prototypeMaxComponents = getNumericComponents(getPrototypeAreaDefinition(areaName, pageNode));
        int componentMaxComponents = getNumericComponents(getTemplateAreaDefinition(areaName, getTemplate(node.getParent().getParent())));
        if (componentMaxComponents != MAX_VALUE) {
            maxComponents = componentMaxComponents;
        } else if (templateMaxComponents != MAX_VALUE) {
            maxComponents = templateMaxComponents;
        } else {
            maxComponents = prototypeMaxComponents;
        }
        return maxComponents;
    }

    private AreaDefinition getTemplateAreaDefinition(final String areaName, final String templateId) {
        AreaDefinition returnValue = null;
        TemplateDefinition templateDefinition;

        try {
            templateDefinition = _templateDefinitionRegistry.getTemplateDefinition(templateId);
            returnValue = templateDefinition != null ? MapUtils.getObject(templateDefinition.getAreas(), areaName) : null;

            if (returnValue == null && templateDefinition != null) {
                // go deeper under first area
                for (final AreaDefinition areaDefinition : templateDefinition.getAreas().values()) {
                    returnValue = MapUtils.getObject(areaDefinition.getAreas(), areaName);
                }
            }
        } catch (RegistrationException e) {
            LOGGER.debug("Unable to get template definition from template id [{}]", templateId, e);
        }

        return returnValue;
    }

    private AreaDefinition getPrototypeAreaDefinition(final String areaName, final Node pageNode) {
        Site site = _multiSiteManager.getAssignedSite(pageNode);
        Iterator<TemplateDefinition> results = Collections.emptyListIterator();
        AreaDefinition area = null;

        if (site != null) {
            STKPage prototype = site.getTemplates().getPrototype();
            if (prototype != null) {
                results = TRAVERSER.breadthFirstTraversal(prototype).filter(new TemplateNamePredicate(areaName)).iterator();
            }
        }

        while (results.hasNext() && area == null) {
            TemplateDefinition def = results.next();
            if (def instanceof AreaDefinition) {
                area = (AreaDefinition) def;
            }
        }

        return area;
    }

    private int getMaxComponentsFromGivenComponent(final Node node) {
        int maxComponents = MAX_VALUE;
        try {
            final TemplateDefinition templateDefinition = _templateDefinitionRegistry.getTemplateDefinition(getTemplate(node));
            if (templateDefinition != null && templateDefinition.getParameters().containsKey(PARAM_MAX_COMPONENTS)) {
                maxComponents = toInt((String) templateDefinition.getParameters().get(PARAM_MAX_COMPONENTS));
            }
        } catch (RegistrationException e) {
            LOGGER.debug("Error on maxComponents of component  [{}].", getPathIfPossible(node), e);
        }
        return maxComponents;
    }

    private int getNumericComponents(final AreaDefinition areaDefinition) {
        return areaDefinition != null && areaDefinition.getMaxComponents() != null ? areaDefinition.getMaxComponents() : MAX_VALUE;
    }

    @Inject
    public void setTemplateDefinitionRegistry(final TemplateDefinitionRegistry templateDefinitionRegistry) {
        _templateDefinitionRegistry = templateDefinitionRegistry;
    }

    @Inject
    public void setMultiSiteManager(final MultiSiteManager multiSiteManager) {
        _multiSiteManager = multiSiteManager;
    }
}
