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

import info.magnolia.jcr.decoration.ContentDecoratorNodeWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SinglePageNodeFilter}.
 *
 * @author wolf.bubenik
 * @since 2025-11-18
 */
class SinglePageNodeFilterTest {

    private SinglePageNodeFilter _filter;

    @BeforeEach
    void setUp() {
        _filter = new SinglePageNodeFilter();
    }

    @Test
    void testWrapNodeWithSinglePageNodeFilteringPredicate() throws RepositoryException {
        SinglePageNodeFilteringPredicate predicate = mock(SinglePageNodeFilteringPredicate.class);
        _filter.setNodePredicate(predicate);

        Node pageNode = mockPageNode("/test/page");

        Node wrappedNode = _filter.wrapNode(pageNode);

        assertInstanceOf(ContentDecoratorNodeWrapper.class, wrappedNode);
        verify(predicate).setBasePageNodePath("/test/page");
    }

    @Test
    void testWrapNodeWithNoPredicate() throws RepositoryException {
        Node pageNode = mockPageNode("/test/page");

        Node wrappedNode = _filter.wrapNode(pageNode);

        assertInstanceOf(ContentDecoratorNodeWrapper.class, wrappedNode);
    }
}

