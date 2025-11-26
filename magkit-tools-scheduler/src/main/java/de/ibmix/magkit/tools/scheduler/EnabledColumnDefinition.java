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
 * Custom column definition for displaying the enabled status of scheduler jobs with a visual tick icon.
 * This column uses an HTML renderer to display a tick icon when a job's "enabled" property is set to true.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Displays a tick icon for enabled jobs</li>
 * <li>Shows empty space for disabled jobs</li>
 * <li>Uses HTML rendering with Vaadin icon elements</li>
 * <li>Reads the "enabled" boolean property from JCR nodes</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * Configured as a column type "enabledColumn" in content app definitions for scheduler job management,
 * providing quick visual feedback about job activation status.
 *
 * @author frank.sommer
 * @since 2023-09-01
 */
@Slf4j
@ColumnType("enabledColumn")
public class EnabledColumnDefinition extends ConfiguredColumnDefinition<Item> {

    /**
     * Creates a new EnabledColumnDefinition with HTML renderer and tick value provider.
     */
    public EnabledColumnDefinition() {
        setRenderer(HtmlRenderer.class);
        setValueProvider(TickValueProvider.class);
    }

    /**
     * Value provider that generates HTML markup for displaying a tick icon when a job is enabled.
     * Reads the "enabled" property from JCR nodes and returns appropriate HTML span elements with icon classes.
     *
     * @author frank.sommer
     * @since 2023-09-01
     */
    public static class TickValueProvider implements ValueProvider<Item, String> {

        /**
         * Generates the HTML markup for the enabled status icon.
         *
         * @param item the JCR item to evaluate
         * @return HTML span element with tick icon if enabled, null otherwise
         */
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

        /**
         * Determines the icon class based on the enabled property of the node.
         *
         * @param node the JCR node to evaluate
         * @return "icon-tick" if enabled property is true, empty string otherwise
         */
        protected String getIcon(Node node) {
            return PropertyUtil.getBoolean(node, "enabled", false) ? "icon-tick" : EMPTY;
        }

    }
}
