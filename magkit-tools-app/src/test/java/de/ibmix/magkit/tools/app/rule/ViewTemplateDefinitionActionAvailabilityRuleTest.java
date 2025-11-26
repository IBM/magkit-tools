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
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ViewTemplateDefinitionActionAvailabilityRule}.
 *
 * @author wolf.bubenik
 * @since 2025-11-17
 */
class ViewTemplateDefinitionActionAvailabilityRuleTest {

    private ViewTemplateDefinitionActionAvailabilityRule _rule;

    @BeforeEach
    void setUp() throws RepositoryException {
        mockAggregationState(stubCharacterEncoding("UTF-8"));
        _rule = new ViewTemplateDefinitionActionAvailabilityRule();
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void isAvailableForItemWithTemplate() throws Exception {
        Node node = mockPageNode("/test/page", stubTemplate("myModule:pages/detail"));
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertTrue(_rule.isAvailableForItem(itemId));
    }

    @Test
    void isAvailableForItemWithoutTemplate() throws Exception {
        Node node = mockNode("website", "/test/page");
        JcrNodeItemId itemId = new JcrNodeItemId(node.getIdentifier(), node.getSession().getWorkspace().getName());

        assertTrue(_rule.isAvailableForItem(itemId));
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
}

