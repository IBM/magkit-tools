package de.ibmix.magkit.tools.scheduler;

/*-
 * #%L
 * IBM iX Magnolia Kit Tools Scheduler
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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
import info.magnolia.jcr.util.PropertyUtil;
import org.apache.commons.lang.time.FastDateFormat;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Item;
import javax.jcr.Node;
import java.text.ParseException;
import java.util.Date;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * <p><strong>Purpose:</strong> Provides the formatted date/time of the next valid execution for a Quartz cron expression stored on a JCR node.</p>
 * <p><strong>Main Functionality:</strong> Reads the <code>cron</code> property from a given {@link Item} (expected to be a {@link Node}) and computes the next run time using {@link CronExpression}. Returns a human readable string (pattern <code>yyyy-MM-dd HH:mm</code>) or an explanatory status message.</p>
 * <p><strong>Key Features:</strong> Stateless, thread-safe provider implementation for Vaadin data binding; handles missing or unparsable cron expressions gracefully; uses a thread-safe {@link FastDateFormat} instance for formatting.</p>
 * <p><strong>Usage Preconditions:</strong> The supplied {@link Item} must be a {@link Node} containing a string property named <code>cron</code> holding a valid Quartz cron expression. If these preconditions are not met, a status message is returned.</p>
 * <p><strong>Null and Error Handling:</strong> If the item is <code>null</code> or not a node, an empty string is returned. If the cron property is absent or blank, the string <code>"not specified"</code> is returned. If the cron expression cannot be parsed, <code>"not parsable"</code> is returned and an error is logged.</p>
 * <p><strong>Side Effects:</strong> No side effects. The evaluation and formatting are read-only operations; only logging on parse failures occurs.</p>
 * <p><strong>Thread-Safety:</strong> This class is thread-safe. It is immutable and uses {@link FastDateFormat} which is documented as thread-safe. A new {@link CronExpression} instance is created per invocation.</p>
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * // In a Vaadin Grid column definition:
 * Grid<Item> grid = new Grid<>();
 * grid.addColumn(new CronNextRunDateProvider())
 *     .setCaption("Next Run");
 * }</pre>
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-25
 */
public class CronNextRunDateProvider implements ValueProvider<Item, String> {
    private static final Logger LOG = LoggerFactory.getLogger(CronNextRunDateProvider.class);

    private static final String CRON_PROPERTY_NAME = "cron";
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");

    /**
     * Returns the formatted next execution time for the cron expression stored under the {@code cron} property
     * of the provided {@link Item} node or a status message if unavailable or invalid.
     *
     * @param item the source item expected to be a {@link Node} containing the cron expression property
     * @return formatted next run date (pattern {@code yyyy-MM-dd HH:mm}), {@code "not specified"} if absent,
     * {@code "not parsable"} if invalid, or empty string if the item is null or not a node
     */
    @Override
    public String apply(Item item) {
        String result = EMPTY;
        if (item != null && item.isNode()) {
            String cronExpression = PropertyUtil.getString((Node) item, CRON_PROPERTY_NAME);
            if (isNotBlank(cronExpression)) {
                try {
                    CronExpression cron = new CronExpression(cronExpression);
                    Date nextValidTime = cron.getNextValidTimeAfter(new Date());
                    if (nextValidTime != null) {
                        result = DATE_FORMAT.format(nextValidTime);
                    }
                } catch (ParseException e) {
                    LOG.error("Cannot parse cron expression '{}': {}", cronExpression, e.getMessage());
                    result = "not parsable";
                }
            } else {
                result = "not specified";
            }
        }
        return result;
    }
}
