package de.ibmix.magkit.scheduler;

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
 * Converter for string to string map.
 *
 * @author frank.sommer
 * @since 01.09.2023
 */
@Slf4j
public class MapConverter implements Converter<String, Map<String, String>> {

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

    @Override
    public String convertToPresentation(Map<String, String> value, ValueContext context) {
        return StringUtils.removeEnd(StringUtils.removeStart(new GsonBuilder().create().toJson(value), "{"), "}");
    }
}
