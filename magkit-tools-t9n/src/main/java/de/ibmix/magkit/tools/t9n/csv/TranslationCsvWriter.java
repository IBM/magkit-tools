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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Create CSV file for all translations.
 *
 * @author diana.racho (IBM iX)
 */
public class TranslationCsvWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationCsvWriter.class);
    private static final String FILE_EXTENSION = ".csv";
    protected static final String COLUMN_KEY = "Key";

    private final File _file;
    private final File _path;

    private final Map<String, Map<String, String>> _eventEntries;
    private Collection<Locale> _locales;

    public TranslationCsvWriter(Map<String, Map<String, String>> entries, File path, Collection<Locale> locales) {
        _path = path;
        _file = createFile();
        _locales = locales;
        _eventEntries = entries;
    }

    /**
     * Export logic.
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

    public FileInputStream getStream() {
        try {
            return new FileInputStream(getFile());
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not stream file.", e);
            return null;
        }
    }

    protected File getFile() {
        return _file;
    }
}
