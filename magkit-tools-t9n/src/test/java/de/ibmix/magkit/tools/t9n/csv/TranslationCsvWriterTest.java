package de.ibmix.magkit.tools.t9n.csv;

/*-
 * #%L
 * magkit-tools-t9n
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

import au.com.bytecode.opencsv.CSVReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TranslationCsvWriter}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-11-19
 */
class TranslationCsvWriterTest {

    @TempDir
    private File _tempDir;

    private TranslationCsvWriter _csvWriter;

    @AfterEach
    void tearDown() {
        if (_csvWriter != null && _csvWriter.getFile() != null) {
            _csvWriter.getFile().delete();
        }
    }

    /**
     * Verifies CSV file is created with header row and translation entries.
     */
    @Test
    void writeCsvCreatesFileWithHeaderAndEntries() throws Exception {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        Map<String, String> trans1 = new HashMap<>();
        trans1.put("translation_en", "Hello");
        trans1.put("translation_de", "Hallo");
        entries.put("greeting", trans1);

        List<Locale> locales = List.of(Locale.ENGLISH, Locale.GERMAN);
        _csvWriter = new TranslationCsvWriter(entries, _tempDir, locales);
        _csvWriter.writeCsv();

        File file = _csvWriter.getFile();
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
            List<String[]> lines = reader.readAll();
            assertEquals(2, lines.size());
            String[] header = lines.get(0);
            assertEquals("Key", header[0]);
            assertEquals(Locale.ENGLISH.getDisplayName(), header[1]);
            assertEquals(Locale.GERMAN.getDisplayName(), header[2]);

            String[] data = lines.get(1);
            assertEquals("greeting", data[0]);
            assertEquals("Hello", data[1]);
            assertEquals("Hallo", data[2]);
        }
    }

    /**
     * Verifies empty entries map produces only header row.
     */
    @Test
    void writeCsvWithEmptyEntriesProducesOnlyHeader() throws Exception {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        List<Locale> locales = List.of(Locale.ENGLISH);
        _csvWriter = new TranslationCsvWriter(entries, _tempDir, locales);
        _csvWriter.writeCsv();

        File file = _csvWriter.getFile();
        assertNotNull(file);
        assertTrue(file.exists());

        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
            List<String[]> lines = reader.readAll();
            assertEquals(1, lines.size());
            assertEquals("Key", lines.get(0)[0]);
        }
    }

    /**
     * Verifies null values in translations are handled gracefully.
     */
    @Test
    void writeCsvHandlesNullTranslationValues() throws Exception {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        Map<String, String> trans1 = new HashMap<>();
        trans1.put("translation_en", "Hello");
        trans1.put("translation_de", null);
        entries.put("greeting", trans1);

        List<Locale> locales = List.of(Locale.ENGLISH, Locale.GERMAN);
        _csvWriter = new TranslationCsvWriter(entries, _tempDir, locales);
        _csvWriter.writeCsv();

        File file = _csvWriter.getFile();
        assertNotNull(file);

        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
            List<String[]> lines = reader.readAll();
            assertEquals(2, lines.size());
            String[] data = lines.get(1);
            assertEquals("greeting", data[0]);
            assertEquals("Hello", data[1]);
            assertEquals("", data[2]);
        }
    }

    /**
     * Verifies getStream returns input stream to the CSV file.
     */
    @Test
    void getStreamReturnsInputStreamToFile() throws Exception {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        List<Locale> locales = List.of(Locale.ENGLISH);
        _csvWriter = new TranslationCsvWriter(entries, _tempDir, locales);
        _csvWriter.writeCsv();

        FileInputStream stream = _csvWriter.getStream();
        assertNotNull(stream);
        stream.close();
    }

    /**
     * Verifies getStream returns null when file does not exist.
     */
    @Test
    void getStreamReturnsNullWhenFileDoesNotExist() {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        List<Locale> locales = List.of(Locale.ENGLISH);
        _csvWriter = new TranslationCsvWriter(entries, _tempDir, locales);

        File file = _csvWriter.getFile();
        if (file != null && file.exists()) {
            file.delete();
        }

        FileInputStream stream = _csvWriter.getStream();
        assertNull(stream);
    }

    /**
     * Verifies multiple locale columns are written in correct order.
     */
    @Test
    void writeCsvHandlesMultipleLocales() throws Exception {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        Map<String, String> trans1 = new HashMap<>();
        trans1.put("translation_en", "Hello");
        trans1.put("translation_de", "Hallo");
        trans1.put("translation_fr", "Bonjour");
        entries.put("greeting", trans1);

        List<Locale> locales = List.of(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH);
        _csvWriter = new TranslationCsvWriter(entries, _tempDir, locales);
        _csvWriter.writeCsv();

        File file = _csvWriter.getFile();
        assertNotNull(file);

        try (CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), UTF_8))) {
            List<String[]> lines = reader.readAll();
            assertEquals(2, lines.size());
            String[] header = lines.get(0);
            assertEquals(4, header.length);
            assertEquals(Locale.FRENCH.getDisplayName(), header[3]);
            String[] data = lines.get(1);
            assertEquals("Bonjour", data[3]);
        }
    }

    /**
     * Verifies getFile returns the created file.
     */
    @Test
    void getFileReturnsCreatedFile() {
        Map<String, Map<String, String>> entries = new TreeMap<>();
        List<Locale> locales = List.of(Locale.ENGLISH);
        _csvWriter = new TranslationCsvWriter(entries, _tempDir, locales);

        File file = _csvWriter.getFile();
        assertNotNull(file);
        assertTrue(file.getName().endsWith(".csv"));
    }
}

