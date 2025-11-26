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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Item;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link CronNextRunDateProvider}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-25
 */
class CronNextRunDateProviderTest {

    private CronNextRunDateProvider _provider;

    @BeforeEach
    void setUp() {
        _provider = new CronNextRunDateProvider();
    }

    @AfterEach
    void tearDown() {
        SessionMockUtils.cleanSession();
    }

    @Test
    void applyReturnsEmptyStringForNullItem() {
        assertEquals("", _provider.apply(null));
    }

    @Test
    void applyReturnsEmptyStringForNonNodeItem() {
        Item item = mock(Item.class);
        assertEquals("", _provider.apply(item));
    }

    @Test
    void applyReturnsNotSpecifiedForMissingCron() throws Exception {
        Item item = mockNode("jobs", "/test/cron/missing");
        assertEquals("not specified", _provider.apply(item));
    }

    @Test
    void applyReturnsNotSpecifiedForEmptyCron() throws Exception {
        Item item = mockNode("jobs", "/test/cron/empty", stubProperty("cron", ""));
        assertEquals("not specified", _provider.apply(item));
    }

    @Test
    void applyReturnsNotSpecifiedForWhitespaceCron() throws Exception {
        Item item = mockNode("jobs", "/test/cron/whitespace", stubProperty("cron", "   \t"));
        assertEquals("not specified", _provider.apply(item));
    }

    @Test
    void applyReturnsFormattedDateForValidCron() throws Exception {
        Item item = mockNode("jobs", "/test/cron/valid", stubProperty("cron", "0 0/1 * ? * *"));
        String result = _provider.apply(item);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"), "Unexpected date format: " + result);
    }

    @Test
    void applyReturnsEmptyStringWhenCronHasNoFutureExecution() throws Exception {
        Item item = mockNode("jobs", "/test/cron/past", stubProperty("cron", "0 0 0 1 1 ? 2000"));
        assertEquals("", _provider.apply(item));
    }

    @Test
    void applyReturnsNotParsableForInvalidCron() throws Exception {
        Item item = mockNode("jobs", "/test/cron/invalid", stubProperty("cron", "invalid cron"));
        assertEquals("not parsable", _provider.apply(item));
    }

    @Test
    void applyReturnsFormattedDateForCronWithWhitespace() throws Exception {
        Item item = mockNode("jobs", "/test/cron/validWhitespace", stubProperty("cron", " 0 0/1 * ? * * "));
        String result = _provider.apply(item);
        assertTrue(result.isEmpty() || result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }

    @Test
    void applyIsThreadSafe() throws Exception {
        Item item = mockNode("jobs", "/test/cron/concurrent", stubProperty("cron", "0 0/1 * ? * *"));
        List<String> results = new CopyOnWriteArrayList<>();
        IntStream.range(0, 25).parallel().forEach(i -> results.add(_provider.apply(item)));
        assertEquals(25, results.size());
        results.forEach(r -> assertTrue(r.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")));
        assertNotNull(results.get(0));
    }
}
