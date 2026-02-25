package de.ibmix.magkit.tools.t9n.rest;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Translation
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
import de.ibmix.magkit.tools.t9n.TranslationNodeTypes;
import info.magnolia.context.MgnlContext;
import info.magnolia.rest.registry.ConfiguredEndpointDefinition;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockSystemContext;
import static de.ibmix.magkit.test.cms.context.SystemContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PN_KEY;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link I18nEndpoint} ensuring translation retrieval, fallback behaviour and error handling.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-20
 */
public class I18nEndpointTest {

    private I18nEndpoint _endpoint;

    @BeforeEach
    public void setUp() {
        ConfiguredEndpointDefinition endpointDefinition = mock(ConfiguredEndpointDefinition.class);
        _endpoint = new I18nEndpoint(endpointDefinition);
        mockSystemContext(stubJcrSession(WS_TRANSLATION));
    }

    @AfterEach
    public void tearDown() {
        ContextMockUtils.cleanContext();
    }

    /**
     * Verifies translation retrieval for a language only locale (e.g. de) using single property name.
     */
    @Test
    public void translateLanguageOnlyLocale() throws RepositoryException {
        mockNode(WS_TRANSLATION, "/greeting",
            stubProperty(PN_KEY, "test.key.greeting"),
            stubProperty(PREFIX_NAME + "de", "Hallo"),
            stubType(TranslationNodeTypes.Translation.NAME)
        );
        Response response = _endpoint.translate("de");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertEquals(1, entity.size());
        assertEquals("Hallo", entity.get("test.key.greeting"));

    }

    /**
     * Verifies fallback to language property when country specific property is missing.
     */
    @Test
    public void translateLocaleWithCountryFallback() throws RepositoryException {
        mockNode(WS_TRANSLATION, "/greet",
            stubProperty(PN_KEY, "greet"),
            stubProperty(PREFIX_NAME + "en", "Hello"),
            stubType(TranslationNodeTypes.Translation.NAME)
        );
        Response response = _endpoint.translate("en_US");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertEquals(1, entity.size());
        assertEquals("Hello", entity.get("greet"));
    }

    /**
     * Verifies usage of country specific property over language property when both exist.
     */
    @Test
    public void translateLocaleWithCountryPreferred() throws RepositoryException {
        mockNode(WS_TRANSLATION, "/welcome",
            stubProperty(PN_KEY, "welcome"),
            stubProperty(PREFIX_NAME + "en", "Hello"),
            stubProperty(PREFIX_NAME + "en_US", "Howdy"),
            stubType(TranslationNodeTypes.Translation.NAME)
        );
        Response response = _endpoint.translate("en_US");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertEquals(1, entity.size());
        assertEquals("Howdy", entity.get("welcome"));
    }

    /**
     * Verifies empty result map when there are no translation nodes.
     */
    @Test
    public void translateReturnsEmptyMapWhenNoNodes() {
        Response response = _endpoint.translate("de");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertTrue(entity.isEmpty());
    }

    /**
     * Verifies error handling returns empty map when a RepositoryException occurs while accessing root node.
     */
    @Test
    public void translateReturnsEmptyMapOnRepositoryException() throws RepositoryException {
        Session session = MgnlContext.getJCRSession(WS_TRANSLATION);
        when(session.getRootNode()).thenThrow(new RepositoryException("failure"));
        Response response = _endpoint.translate("en");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertTrue(entity.isEmpty());
    }

    /**
     * Verifies that the result map is sorted by key (TreeMap implementation) independent from insertion order.
     */
    @Test
    public void translateReturnsSortedKeys() throws RepositoryException {
        mockNode(WS_TRANSLATION, "/bNode",
            stubProperty(PN_KEY, "b.key"),
            stubProperty(PREFIX_NAME + "de", "B"),
            stubType(TranslationNodeTypes.Translation.NAME)
        );
        mockNode(WS_TRANSLATION, "/aNode",
            stubProperty(PN_KEY, "a.key"),
            stubProperty(PREFIX_NAME + "de", "A"),
            stubType(TranslationNodeTypes.Translation.NAME)
        );

        Response response = _endpoint.translate("de");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<String, String> entity = (Map<String, String>) response.getEntity();
        List<String> keys = new ArrayList<>(entity.keySet());
        assertEquals("a.key", keys.get(0));
        assertEquals("b.key", keys.get(1));
    }

    /**
     * Verifies empty result map when there are only nodes with wrong or missing node type.
     */
    @Test
    public void translateReturnsOnlyNodesWithTranslationNodeType() throws RepositoryException {
        mockNode(WS_TRANSLATION, "/missing",
            stubProperty(PN_KEY, "missing"),
            stubProperty(PREFIX_NAME + "de", "missing node type")
        );
        mockNode(WS_TRANSLATION, "/wrongNodeType",
            stubProperty(PN_KEY, "wrongNodeType"),
            stubProperty(PREFIX_NAME + "de", "wrong node type"),
            stubType("wrongNodeType")
        );
        Response response = _endpoint.translate("de");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertTrue(entity.isEmpty());
    }
}
