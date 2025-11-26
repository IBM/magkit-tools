package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * magkit-tools-scheduler
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

import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MapConverter}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-19
 */
class MapConverterTest {

    private final MapConverter _converter = new MapConverter();
    private final ValueContext _ctx = new ValueContext();

    /**
     * Verifies convertToModel returns Result.ok(null) for null input.
     */
    @Test
    void convertToModelReturnsNullResultOnNullInput() {
        Result<Map<String, String>> result = _converter.convertToModel(null, _ctx);
        assertFalse(result.isError());
        assertNull(result.getOrThrow(r -> new IllegalStateException("Unexpected error")));
    }

    /**
     * Verifies empty string parses to an empty map.
     */
    @Test
    void convertToModelParsesEmptyStringAsEmptyMap() {
        Result<Map<String, String>> result = _converter.convertToModel("", _ctx);
        Map<String, String> map = result.getOrThrow(r -> new IllegalStateException("Unexpected error"));
        assertEquals(0, map.size());
    }

    /**
     * Verifies valid JSON key/value pairs produce a map with expected quoting.
     */
    @Test
    void convertToModelParsesValidJson() {
        Result<Map<String, String>> result = _converter.convertToModel("\"a\":\"1\", \"b\":2", _ctx);
        Map<String, String> map = result.getOrThrow(r -> new IllegalStateException("Unexpected error"));
        assertEquals(2, map.size());
        assertEquals("\"1\"", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    /**
     * Verifies invalid JSON returns an error Result.
     */
    @Test
    void convertToModelReturnsErrorOnInvalidJson() {
        Result<Map<String, String>> result = _converter.convertToModel("\"a\":", _ctx);
        assertTrue(result.isError());
    }

    /**
     * Verifies null map presentation returns literal string "null" (Gson behavior).
     */
    @Test
    void convertToPresentationReturnsNullStringForNullMap() {
        String json = _converter.convertToPresentation(null, _ctx);
        assertEquals("null", json);
    }

    /**
     * Verifies empty map serializes to empty string without braces.
     */
    @Test
    void convertToPresentationSerializesEmptyMapToEmptyString() {
        String json = _converter.convertToPresentation(new TreeMap<>(), _ctx);
        assertEquals("", json);
    }

    /**
     * Verifies ordered serialization without braces for a populated map.
     */
    @Test
    void convertToPresentationSerializesMapWithoutBraces() {
        TreeMap<String, String> map = new TreeMap<>();
        map.put("b", "2");
        map.put("a", "1");
        String json = _converter.convertToPresentation(map, _ctx);
        assertEquals("\"a\":\"1\",\"b\":\"2\"", json);
    }

    /**
     * Verifies values with spaces retain quoted representation.
     */
    @Test
    void convertToModelParsesValuesPreservingQuotes() {
        Result<Map<String, String>> result = _converter.convertToModel("\"quoted\":\"value with spaces\"", _ctx);
        Map<String, String> map = result.getOrThrow(r -> new IllegalStateException("Unexpected error"));
        assertEquals("\"value with spaces\"", map.get("quoted"));
    }
}
