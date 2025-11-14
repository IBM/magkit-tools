package de.ibmix.magkit.tools.app;

/*-
 * #%L
 * magkit-tools-app
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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for QueryResultTable column name normalization.
 *
 * @author frank.sommer
 * @since 2017-12-05
 */
public class QueryResultTableTest {
    @Test
    public void normalizeColumnName() {
        QueryResultTable resultTable = new QueryResultTable();
        assertEquals("test", resultTable.normalizeColumnName("test"));
        assertEquals("test.jcr:type", resultTable.normalizeColumnName("test.jcr:type"));
        assertEquals("test", resultTable.normalizeColumnName("mgnl:page.test"));
        assertEquals("jcr:type", resultTable.normalizeColumnName("mgnl:page.jcr:type"));
    }
}
