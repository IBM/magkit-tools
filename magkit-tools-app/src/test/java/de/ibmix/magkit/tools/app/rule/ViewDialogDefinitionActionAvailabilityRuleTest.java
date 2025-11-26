package de.ibmix.magkit.tools.app.rule;

/*-
 * #%L
 * magkit-tools-app
 * %%
 * Copyright (C) 2023 IBM iX
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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.cms.templating.TemplateMockUtils;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCharacterEncoding;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDialog;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ViewDialogDefinitionActionAvailabilityRule}.
 *
 * @author wolf.bubenik
 * @since 2025-11-17
 */
class ViewDialogDefinitionActionAvailabilityRuleTest {

    private TemplateDefinitionRegistry _templateRegistry;
    private ViewDialogDefinitionActionAvailabilityRule _rule;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        _templateRegistry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        _rule = new ViewDialogDefinitionActionAvailabilityRule();
        _rule.setTemplateRegistry(_templateRegistry);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void isAvailableForItemWithValidDialogDefinition() throws Exception {
        Node node = mockPageNode("/test/page", stubTemplate("myModule:pages/detail", stubDialog("myModule:dialogs/testDialog")));
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertTrue(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithoutDialogDefinition() throws Exception {
        Node node = mockPageNode("/test/page", stubTemplate("myModule:pages/detail"));
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertFalse(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithEmptyDialogDefinition() throws Exception {
        Node node = mockPageNode("/test/page", stubTemplate("myModule:pages/detail", stubDialog("")));
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertFalse(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithoutTemplate() throws Exception {
        Node node = mockNode("website", "/test/page");
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertFalse(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithNullTemplateRegistry() throws Exception {
        Node node = mockPageNode("/test/page", stubTemplate("myModule:pages/detail", stubDialog("myModule:dialogs/testDialog")));
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());
        _rule.setTemplateRegistry(null);

        assertTrue(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithInvalidTemplate() throws Exception {
        Node node = mockPageNode("/test/page", stubTemplate("invalid:template"));
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertFalse(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithNonJcrNodeItemId() {
        String itemId = "not-a-jcr-node-item-id";

        assertFalse(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithNullItemId() {
        assertFalse(_rule.isAvailableForItem(null));
    }

    @Test
    void setTemplateRegistry() {
        _rule.setTemplateRegistry(_templateRegistry);
    }

    @Test
    void getTemplateRegistryLazyLoadingImplicitlyTested() throws Exception {
        ViewDialogDefinitionActionAvailabilityRule rule = new ViewDialogDefinitionActionAvailabilityRule();
        Node node = mockPageNode("/test/page", stubTemplate("myModule:pages/detail", stubDialog("myModule:dialogs/testDialog")));
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertTrue(rule.isAvailableForItem(itemId));
    }
}

