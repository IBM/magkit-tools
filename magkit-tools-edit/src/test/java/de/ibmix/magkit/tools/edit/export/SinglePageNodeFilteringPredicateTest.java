package de.ibmix.magkit.tools.edit.export;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Edit
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
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockContentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SinglePageNodeFilteringPredicate}.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class SinglePageNodeFilteringPredicateTest {

    private SinglePageNodeFilteringPredicate _predicate;

    @BeforeEach
    void setUp() {
        _predicate = new SinglePageNodeFilteringPredicate();
    }

    @AfterEach
    void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void testEvaluateTypedWithNonPageNode() throws RepositoryException {
        Node contentNode = mockContentNode("/test/page/content");
        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(contentNode);

        assertTrue(result);
    }

    @Test
    void testEvaluateTypedWithPageNodeMatchingPath() throws RepositoryException {
        Node pageNode = mockPageNode("/test/page");
        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(pageNode);

        assertTrue(result);
    }

    @Test
    void testEvaluateTypedWithPageNodeMatchingChildPath() throws RepositoryException {
        Node pageNode = mockPageNode("/test/page");
        _predicate.setBasePageNodePath("/test/page/child");

        boolean result = _predicate.evaluateTyped(pageNode);

        assertTrue(result);
    }

    @Test
    void testEvaluateTypedWithPageNodeNotMatchingPath() throws RepositoryException {
        Node pageNode = mockPageNode("/test/other");
        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(pageNode);

        assertFalse(result);
    }

    @Test
    void testEvaluateTypedWithPageNodeParentPath() throws RepositoryException {
        Node pageNode = mockPageNode("/test/page/child");
        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(pageNode);

        assertFalse(result);
    }

    @Test
    void testEvaluateTypedWithVariantPageNode() throws RepositoryException {
        Node variantNode = mockPageNode("/test/page/variants/variant1");
        NodeType[] variantNodeTypes = createVariantNodeTypes();
        when(variantNode.getMixinNodeTypes()).thenReturn(variantNodeTypes);

        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(variantNode);

        assertTrue(result);
    }

    @Test
    void testEvaluateTypedWithVariantPageNodeMatchingChild() throws RepositoryException {
        Node variantNode = mockPageNode("/test/page/variants/variant1");
        NodeType[] variantNodeTypes = createVariantNodeTypes();
        when(variantNode.getMixinNodeTypes()).thenReturn(variantNodeTypes);

        _predicate.setBasePageNodePath("/test/page/child");

        boolean result = _predicate.evaluateTyped(variantNode);

        assertTrue(result);
    }

    @Test
    void testEvaluateTypedWithVariantPageNodeNotMatching() throws RepositoryException {
        Node variantNode = mockPageNode("/test/other/variants/variant1");
        NodeType[] variantNodeTypes = createVariantNodeTypes();
        when(variantNode.getMixinNodeTypes()).thenReturn(variantNodeTypes);

        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(variantNode);

        assertFalse(result);
    }

    @Test
    void testEvaluateTypedWithRepositoryException() throws RepositoryException {
        Node pageNode = mockPageNode("/test/page");
        when(pageNode.isNodeType(NodeTypes.Page.NAME)).thenThrow(new RepositoryException("Test exception"));

        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(pageNode);

        assertFalse(result);
    }

    @Test
    void testEvaluateTypedWithRootPageNode() throws RepositoryException {
        Node rootPageNode = mockPageNode("/rootPage");
        _predicate.setBasePageNodePath("/rootPage");

        boolean result = _predicate.evaluateTyped(rootPageNode);

        assertTrue(result);
    }

    @Test
    void testEvaluateTypedWithSimilarButDifferentPath() throws RepositoryException {
        Node pageNode = mockPageNode("/test/page1");
        _predicate.setBasePageNodePath("/test/page");

        boolean result = _predicate.evaluateTyped(pageNode);

        assertFalse(result);
    }

    NodeType[] createVariantNodeTypes() {
        NodeType variant = mock(NodeType.class);
        when(variant.getName()).thenReturn("mgnl:variant");
        return new javax.jcr.nodetype.NodeType[] {variant};
    }
}

