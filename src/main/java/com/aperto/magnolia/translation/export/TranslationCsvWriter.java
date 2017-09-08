package com.aperto.magnolia.translation.export;

import au.com.bytecode.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;

/**
 * Create CSV file for all translations.
 *
 * @author diana.racho (Aperto AG)
 */
public class TranslationCsvWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationCsvWriter.class);
    private static final String FILE_EXTENSION = ".csv";

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
        headerLine.add("Key");
        for (Locale locale : _locales) {
            headerLine.add(locale.getDisplayName());
        }
        entries.add(headerLine.toArray(new String[headerLine.size()]));
        for (Map.Entry<String, Map<String, String>> entry : _eventEntries.entrySet()) {
            List<String> line = new ArrayList<>();
            line.add(entry.getKey());
            for (Locale locale : _locales) {
                line.add(entry.getValue().get(PREFIX_NAME + locale.getLanguage()));
            }
            entries.add(line.toArray(new String[line.size()]));
        }

        try (
            FileWriter fileWriter = new FileWriter(getFile());
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