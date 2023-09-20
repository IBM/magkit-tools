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

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.renderers.HtmlRenderer;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.contentapp.configuration.column.ColumnType;
import info.magnolia.ui.contentapp.configuration.column.ConfiguredColumnDefinition;
import lombok.extern.slf4j.Slf4j;

import javax.jcr.Item;
import javax.jcr.Node;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Column definition that shows a tick if enabled property is true.
 *
 * @author frank.sommer
 * @since 01.09.2023
 */
@Slf4j
@ColumnType("enabledColumn")
public class EnabledColumnDefinition extends ConfiguredColumnDefinition<Item> {

    public EnabledColumnDefinition() {
        setRenderer(HtmlRenderer.class);
        setValueProvider(TickValueProvider.class);
    }

    /**
     * Value provider setting the tick in the column.
     *
     * @author frank.sommer
     * @since 01.09.2023
     */
    public static class TickValueProvider implements ValueProvider<Item, String> {

        @Override
        public String apply(Item item) {
            if (item.isNode()) {
                String icon = getIcon((Node) item);
                if (isNotBlank(icon)) {
                    return "<span class=\"v-table-icon-element " + icon + "\" ></span>";
                }
            }
            return null;
        }

        protected String getIcon(Node node) {
            return PropertyUtil.getBoolean(node, "enabled", false) ? "icon-tick" : EMPTY;
        }

    }
}
