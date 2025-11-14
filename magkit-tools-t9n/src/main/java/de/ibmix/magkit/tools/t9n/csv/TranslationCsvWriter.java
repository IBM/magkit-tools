package de.ibmix.magkit.tools.t9n.csv;

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

import au.com.bytecode.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.ibmix.magkit.tools.t9n.TranslationNodeTypes.Translation.PREFIX_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility class for exporting translation data to CSV format.
 * <p>
 * <p><strong>Purpose:</strong></p>
 * Creates CSV files containing translation keys and their values for all configured locales,
 * enabling editors to work with translations in external tools like spreadsheet applications.
 * <p>
 * <p><strong>Key Features:</strong></p>
 * <ul>
 * <li>Generates CSV files with UTF-8 encoding</li>
 * <li>Creates header row with "Key" and locale display names</li>
 * <li>Exports all translation entries in alphabetical order</li>
 * <li>Handles missing translations gracefully with empty values</li>
 * <li>Provides access to the generated file as a stream</li>
 * </ul>
 * <p>
 * <p><strong>CSV Format:</strong></p>
 * The first row contains column headers (Key, followed by locale names).
 * Each subsequent row contains a translation key and its values for each locale.
 *
 * @author diana.racho (IBM iX)
 * @since 2023-01-01
 */
@Slf4j
public class TranslationCsvWriter {
    private static final String FILE_EXTENSION = ".csv";
    protected static final String COLUMN_KEY = "Key";

    private final File _file;
    private final File _path;

    private final Map<String, Map<String, String>> _eventEntries;
    private final Collection<Locale> _locales;

    /**
     * Creates a new CSV writer for the given translation data.
     *
     * @param entries map of translation keys to locale-value mappings
     * @param path the directory where the temporary CSV file will be created
     * @param locales the collection of locales to include as columns
     */
    public TranslationCsvWriter(Map<String, Map<String, String>> entries, File path, Collection<Locale> locales) {
        _path = path;
        _file = createFile();
        _locales = locales;
        _eventEntries = entries;
    }

    /**
     * Writes the translation data to the CSV file with UTF-8 encoding.
     * Creates a header row and one row per translation entry.
     */
    public void writeCsv() {
        List<String[]> entries = new ArrayList<>();
        List<String> headerLine = new ArrayList<>();
        headerLine.add(COLUMN_KEY);
        for (Locale locale : _locales) {
            headerLine.add(locale.getDisplayName());
        }
        entries.add(headerLine.toArray(new String[0]));
        for (Map.Entry<String, Map<String, String>> entry : _eventEntries.entrySet()) {
            List<String> line = new ArrayList<>();
            line.add(entry.getKey());
            for (Locale locale : _locales) {
                line.add(entry.getValue().get(PREFIX_NAME + locale.getLanguage()));
            }
            entries.add(line.toArray(new String[0]));
        }

        try (
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(getFile()), UTF_8);
            CSVWriter writer = new CSVWriter(fileWriter)
        ) {
            writer.writeAll(entries);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Could not create csv file", e);
        }
    }

    private File createFile() {
        File file = null;
        try {
            file = File.createTempFile("export", FILE_EXTENSION, _path);
        } catch (IOException e) {
            LOGGER.error("Could not create file.", e);
        }
        return file;
    }

    /**
     * Provides an input stream to the generated CSV file for downloading.
     *
     * @return an input stream to the CSV file, or null if the file cannot be read
     */
    public FileInputStream getStream() {
        try {
            return new FileInputStream(getFile());
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not stream file.", e);
            return null;
        }
    }

    /**
     * Returns the generated CSV file.
     *
     * @return the CSV file
     */
    protected File getFile() {
        return _file;
    }
}
