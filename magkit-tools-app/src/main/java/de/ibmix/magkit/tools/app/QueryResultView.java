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

import javax.jcr.query.QueryResult;

/**
 * View interface for displaying JCR query results.
 * <p><strong>Main Functionalities:</strong></p>
 * <ul>
 *   <li>Displays query results in a tabular format</li>
 *   <li>Shows query execution time</li>
 *   <li>Supports optional display of scores and column values</li>
 * </ul>
 *
 * @author frank.sommer
 * @since 1.5.0
 */
public interface QueryResultView extends ResultView {
    /**
     * Builds and displays the query result table.
     *
     * @param queryResult the JCR query result to display
     * @param showScore whether to display score information
     * @param showCols whether to display all columns
     * @param duration the query execution time in milliseconds
     */
    void buildResultTable(QueryResult queryResult, boolean showScore, boolean showCols, long duration);
}
