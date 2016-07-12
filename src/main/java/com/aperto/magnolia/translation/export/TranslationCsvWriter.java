package com.aperto.magnolia.translation.export;

import au.com.bytecode.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.aperto.magnolia.translation.TranslationNodeTypes.Translation.PREFIX_NAME;

/**
 * Create CSV file for all translations.
 *
 * @author diana.racho (Aperto AG)
 */
public class TranslationCsvWriter {

    private static final String FILE_EXTENSION = ".csv";
    private final File _file;
    private FileInputStream _inputStream;

    private final File _path;

    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationCsvWriter.class);

    private final Map<String, Map<String, String>> _eventEntries;
    private Collection<Locale> _locales;

    public TranslationCsvWriter(Map<String, Map<String, String>> entries, File path, Collection<Locale> locales) {
        _path = path;
        _file = createFile(FILE_EXTENSION);

        _locales = locales;
        _eventEntries = entries;
    }

    /**
     * Export logic.
     */
    public boolean createFile() {
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
        try {
            FileWriter fileWriter = new FileWriter(getFile());
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(entries);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.error("Could not create csv file", e);
        }
        return false;
    }

    private File createFile(final String ext) {
        File file = null;
        try {
            file = File.createTempFile("export", ext, _path);
        } catch (IOException e) {
            LOGGER.error("could not create file", e);
        }
        return file;
    }

    public FileInputStream getStream() {
        if (_inputStream == null) {
            try {
                _inputStream = new FileInputStream(getFile());
            } catch (FileNotFoundException e) {
                LOGGER.error("could not close stream", e);
            }
        }
        return _inputStream;
    }

    File getFile() {
        return _file;
    }
}