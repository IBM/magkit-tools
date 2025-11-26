package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * magkit-scheduler
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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.TreeMap;

/**
 * Vaadin converter for bidirectional conversion between JSON string representation and Map&lt;String, String&gt;.
 * This converter enables editing of key-value parameter maps in Vaadin form fields for scheduler job configurations.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Converts JSON-formatted strings to Map&lt;String, String&gt; objects</li>
 * <li>Converts Map&lt;String, String&gt; objects back to JSON string format</li>
 * <li>Uses TreeMap to maintain sorted order of keys</li>
 * <li>Automatically wraps input in curly braces for JSON parsing</li>
 * <li>Strips outer braces from presentation format</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * Invalid JSON syntax results in an error result with descriptive message logged at error level.
 *
 * <p><strong>Null Handling:</strong></p>
 * Null values in model conversion return Result.ok(null).
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 * // Input string: "key1": "value1", "key2": "value2"
 * // Output map: {key1=value1, key2=value2}
 * </pre>
 *
 * @author frank.sommer
 * @since 2023-09-01
 */
@Slf4j
public class MapConverter implements Converter<String, Map<String, String>> {

    /**
     * Converts a JSON string to a Map&lt;String, String&gt;.
     * The input string should contain JSON key-value pairs without outer curly braces.
     *
     * @param value the JSON string to convert (may be null)
     * @param context the value context for the conversion
     * @return a Result containing the parsed map or an error if JSON parsing fails
     */
    @Override
    public Result<Map<String, String>> convertToModel(String value, ValueContext context) {
        Result<Map<String, String>> result = Result.ok(null);
        if (value != null) {
            try {
                Map<String, String> stringMap = new TreeMap<>();
                final JsonElement jsonElement = JsonParser.parseString("{" + value.trim() + "}");
                jsonElement.getAsJsonObject().entrySet().forEach(entry -> stringMap.put(entry.getKey(), entry.getValue().toString()));
                result = Result.ok(stringMap);
            } catch (JsonSyntaxException e) {
                LOGGER.error("Error parsing json {}.", value, e);
                result = Result.error("Invalid JSON syntax.");
            }
        }
        return result;
    }

    /**
     * Converts a Map&lt;String, String&gt; to its JSON string representation.
     * The output string has the outer curly braces removed for cleaner display in form fields.
     *
     * @param value the map to convert (may be null)
     * @param context the value context for the conversion
     * @return the JSON string representation without outer braces
     */
    @Override
    public String convertToPresentation(Map<String, String> value, ValueContext context) {
        return StringUtils.removeEnd(StringUtils.removeStart(new GsonBuilder().create().toJson(value), "{"), "}");
    }
}
