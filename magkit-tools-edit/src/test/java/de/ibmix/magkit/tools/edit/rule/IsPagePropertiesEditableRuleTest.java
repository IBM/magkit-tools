package de.ibmix.magkit.tools.edit.rule;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
 * %%
 * Copyright (C) 2025 IBM iX
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
import info.magnolia.pages.app.detail.PageEditorStatus;
import info.magnolia.pages.app.detail.action.availability.IsElementEditableRuleDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.ui.api.availability.AvailabilityDefinition;
import info.magnolia.ui.vaadin.gwt.client.shared.AbstractElement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.PageNodeStubbingOperation.stubTemplate;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDialog;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubEditable;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link IsPagePropertiesEditableRule} covering all logical branches of isAvailableFor.
 * <p/>
 * Scenarios:
 * <ul>
 * <li>No ancestor page found - returns false</li>
 * <li>Template dialog missing - returns false</li>
 * <li>Template dialog present & editable null - returns true</li>
 * <li>Template dialog present & editable true - returns true</li>
 * <li>Template dialog present & editable false - returns false</li>
 * </ul>
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class IsPagePropertiesEditableRuleTest {

    private IsPagePropertiesEditableRule _rule;
    private PageEditorStatus _pageEditorStatus;
    private TemplateDefinitionRegistry _templateDefinitionRegistry;

    @BeforeEach
    void setUp() {
        _pageEditorStatus = mock(PageEditorStatus.class);
        _templateDefinitionRegistry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        AvailabilityDefinition availabilityDefinition = mock(AvailabilityDefinition.class);
        IsElementEditableRuleDefinition ruleDefinition = mock(IsElementEditableRuleDefinition.class);
        _rule = new IsPagePropertiesEditableRule(availabilityDefinition, ruleDefinition, _pageEditorStatus, _templateDefinitionRegistry);
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void testNoPageAncestorReturnsFalse() throws RepositoryException {
        mockPageNode("/test");
        when(_pageEditorStatus.getNodePath()).thenReturn("/non/existing/path");
        AbstractElement element = mock(AbstractElement.class);
        boolean available = _rule.isAvailableFor(element);
        assertFalse(available);
    }

    @Test
    void testTemplateDialogMissingReturnsFalse() throws Exception {
        mockPageNode("/test/page", stubTemplate("myTemplate"));
        when(_pageEditorStatus.getNodePath()).thenReturn("/test/page");
        AbstractElement element = mock(AbstractElement.class);
        boolean available = _rule.isAvailableFor(element);
        assertFalse(available);
    }

    @Test
    void testTemplateDialogPresentEditableNullReturnsTrue() throws Exception {
        mockPageNode("/test/page2", stubTemplate("myTemplate2", stubDialog("dialogName")));
        when(_pageEditorStatus.getNodePath()).thenReturn("/test/page2");
        TemplateDefinition templateDefinition = _templateDefinitionRegistry.getProvider("myTemplate2").get();
        when(templateDefinition.getEditable()).thenReturn(null);
        AbstractElement element = mock(AbstractElement.class);
        boolean available = _rule.isAvailableFor(element);
        assertTrue(available);
    }

    @Test
    void testTemplateDialogPresentEditableTrueReturnsTrue() throws Exception {
        mockPageNode("/test/page3", stubTemplate("myTemplate3", stubDialog("dialogName"), stubEditable(true)));
        when(_pageEditorStatus.getNodePath()).thenReturn("/test/page3");
        AbstractElement element = mock(AbstractElement.class);
        boolean available = _rule.isAvailableFor(element);
        assertTrue(available);
    }

    @Test
    void testTemplateDialogPresentEditableFalseReturnsFalse() throws Exception {
        mockPageNode("/test/page4", stubTemplate("myTemplate4", stubDialog("dialogName"), stubEditable(false)));
        when(_pageEditorStatus.getNodePath()).thenReturn("/test/page4");
        AbstractElement element = mock(AbstractElement.class);
        boolean available = _rule.isAvailableFor(element);
        assertFalse(available);
    }
}
