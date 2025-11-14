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
import info.magnolia.rest.registry.ConfiguredEndpointDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.WS_TRANSLATION;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

/**
 * REST endpoint for accessing internationalization translations from the Magnolia translation workspace.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * Provides a RESTful API to retrieve all translation key-value pairs for a specific locale,
 * enabling frontend applications to access localized content dynamically.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Exposes translations via REST API at /i18n/v1/{locale}</li>
 * <li>Supports language-only (e.g., "de") and language-country (e.g., "de_DE") locale formats</li>
 * <li>Returns translations as JSON key-value pairs</li>
 * <li>Falls back to language-only translations when country-specific ones are not available</li>
 * <li>Executes queries in system context for consistent access</li>
 * </ul>
 * <p>
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * GET /rest/i18n/v1/de
 * GET /rest/i18n/v1/en_US
 * </pre>
 * <p>
 * <p><strong>Thread Safety:</strong></p>
 * This endpoint is thread-safe as each request operates within its own context.
 *
 * @author frank.sommer
 * @since 2022-07-13
 */
@Path("/i18n/v1")
@Tag(
    name = "Translations API"
)
@Slf4j
public class I18nEndpoint extends AbstractEndpoint<ConfiguredEndpointDefinition> {

    /**
     * Creates a new I18N endpoint with the given configuration.
     *
     * @param endpointDefinition the endpoint configuration definition
     */
    @Inject
    protected I18nEndpoint(ConfiguredEndpointDefinition endpointDefinition) {
        super(endpointDefinition);
    }

    /**
     * Retrieves all available translations for the specified locale.
     * Returns a JSON object containing key-value pairs where keys are translation identifiers
     * and values are the localized text. Supports fallback from country-specific to language-only translations.
     *
     * @param locale the locale string in ISO format (e.g., "de" or "de_DE")
     * @return a Response containing the translation key-value pairs as JSON
     */
    @Path("/{locale:[a-z]{2}(_[A-Z]{2})?}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get available translations for a locale.")
    public Response translate(@Parameter(description = "A locale containing the ISO-639 language code and optional an ISO-3166 country code. Possible values are 'de' or 'de_DE'.", example = "en", required = true) @PathParam("locale") String locale) {

        Map<String, String> labels = MgnlContext.doInSystemContext(() -> {
            Map<String, String> keyValues = new TreeMap<>();
            try {
                final var jcrSession = MgnlContext.getJCRSession(WS_TRANSLATION);
                final var rootNode = jcrSession.getRootNode();
                final var nodes = NodeUtil.asList(NodeUtil.getNodes(rootNode, new NodeTypePredicate(TranslationNodeTypes.Translation.NAME)));
                final Locale asLocale = new Locale(substringBefore(locale, "_"), substringAfter(locale, "_"));
                final String[] propertyNames = TranslationNodeTypes.Translation.LOCALE_TO_PROPERTY_NAMES.apply(asLocale);

                nodes.forEach(n -> keyValues.put(getString(n, TranslationNodeTypes.Translation.PN_KEY), TranslationNodeTypes.Translation.retrieveValue(n, propertyNames)));
            } catch (RepositoryException e) {
                LOGGER.error("Error getting translation labels.", e);
            }
            return keyValues;
        });

        return Response.ok().entity(labels).build();
    }
}
