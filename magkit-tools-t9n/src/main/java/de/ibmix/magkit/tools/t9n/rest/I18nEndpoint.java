package de.ibmix.magkit.tools.t9n.rest;

/*-
 * #%L
 * magkit-tools-t9n
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

import de.ibmix.magkit.tools.t9n.TranslationNodeTypes;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.DynamicPath;
import info.magnolia.rest.registry.ConfiguredEndpointDefinition;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.TreeMap;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * I18N endpoint for translations from the translation app.
 *
 * @author frank.sommer
 * @since 13.07.2022
 */
@DynamicPath
@Path("/")
@Slf4j
public class I18nEndpoint extends AbstractEndpoint<ConfiguredEndpointDefinition> {

    @Inject
    protected I18nEndpoint(ConfiguredEndpointDefinition endpointDefinition) {
        super(endpointDefinition);
    }

    @Path("/{language:[a-z]{2}}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response translate(@PathParam("language") String language) {
        Response response;

        Map<String, String> labels = MgnlContext.doInSystemContext(() -> {
            Map<String, String> keyValues = new TreeMap<>();
            try {
                final var jcrSession = MgnlContext.getJCRSession(WS_TRANSLATION);
                final var rootNode = jcrSession.getRootNode();
                final var nodes = NodeUtil.asList(NodeUtil.getNodes(rootNode, new NodeTypePredicate(TranslationNodeTypes.Translation.NAME)));
                nodes.forEach(n -> keyValues.put(getString(n, TranslationNodeTypes.Translation.PN_KEY), getString(n, PREFIX_NAME + language, EMPTY)));
            } catch (RepositoryException e) {
                LOGGER.error("Error getting translation labels.", e);
            }
            return keyValues;
        });

        response = Response.ok().entity(labels).build();

        return response;
    }
}
