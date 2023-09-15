package com.aperto.magnolia.edittools.m6.command;

import com.aperto.magnolia.edittools.export.YamlSinglePageExporter;
import info.magnolia.context.Context;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.importexport.command.JcrExportCommand;
import info.magnolia.importexport.command.JcrImportCommand;
import info.magnolia.importexport.contenthandler.YamlContentHandler;
import info.magnolia.importexport.filters.NamespaceFilter;
import info.magnolia.jcr.decoration.ContentDecorator;
import info.magnolia.objectfactory.Classes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.jackrabbit.commons.xml.Exporter;
import org.xml.sax.ContentHandler;

import javax.jcr.Node;
import javax.jcr.Session;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author ala.abudheileh
 * 06.09.2023
 */
public class JcrExportSinglePageCommand extends JcrImportCommand {

    private OutputStream _outputStream;
    private JcrExportSinglePageCommand.Format _format = JcrExportSinglePageCommand.Format.YAML;
    private JcrExportSinglePageCommand.Compression _compression = JcrExportSinglePageCommand.Compression.NONE;
    private Map<String, ContentDecorator> _filters = new HashMap<>();
    private Class<? extends Exporter> _exporterClass;
    private boolean _prettyPrint = SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_10);

    @Override
    public boolean execute(Context context) throws Exception {
        String pathName = DataTransporter.createExportPath(getPath());
        pathName = DataTransporter.encodePath(pathName, DataTransporter.DOT, DataTransporter.UTF8);
        if (DataTransporter.DOT.equals(pathName)) {
            pathName = StringUtils.EMPTY;
        }
        OutputStream compressionOutputStream = getOutputStream();
        String format = getFormat();
        final String fileName = getRepository() + pathName;

        switch (_compression) {
            case ZIP:
                final ZipOutputStream zipOutputStream = new ZipOutputStream(compressionOutputStream);
                zipOutputStream.putNextEntry(new ZipEntry(fileName + "." + format));
                compressionOutputStream = zipOutputStream;
                format = getCompression();
                break;
            case GZ:
                format += "." + getCompression();
                compressionOutputStream = new GZIPOutputStream(compressionOutputStream);
                break;
            default:
                break;
        }
        setFileName(fileName + "." + format);

        try {
            final Session session = context.getJCRSession(getRepository());
            final ContentHandler contentHandler = _format.getContentHandler(compressionOutputStream, isPrettyPrint());

            final Exporter exporter = Classes.getClassFactory().newInstance(getExporterClass(), session, contentHandler, true, true);
            final ContentDecorator contentDecorator = _filters.containsKey(getRepository()) ? _filters.get(getRepository()) : new JcrExportCommand.DefaultFilter();
            final Node node = contentDecorator.wrapNode(getJCRNode(context));

            exporter.export(node);
        } finally {
            // finish the stream properly if zip stream, this is not done by the IOUtils
            if (compressionOutputStream instanceof DeflaterOutputStream) {
                ((DeflaterOutputStream) compressionOutputStream).finish();
            }
            compressionOutputStream.flush();
            IOUtils.closeQuietly(compressionOutputStream);
        }
        return true;
    }

    public OutputStream getOutputStream() {
        return _outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        _outputStream = outputStream;
    }

    public String getFormat() {
        return _format.name().toLowerCase();
    }

    public void setFormat(String format) {
        _format = JcrExportSinglePageCommand.Format.valueOf(format.toUpperCase());
    }

    public String getCompression() {
        return _compression.name().toLowerCase();
    }

    public void setCompression(String compression) {
        _compression = JcrExportSinglePageCommand.Compression.valueOf(compression.toUpperCase());
    }

    public Map<String, ContentDecorator> getFilters() {
        return _filters;
    }

    public void setFilters(Map<String, ContentDecorator> filters) {
        _filters = filters;
    }

    public Class<? extends Exporter> getExporterClass() {
        return _exporterClass == null ? _format.getDefaultExporterClass() : _exporterClass;
    }

    public void setExporterClass(Class<? extends Exporter> exporterClass) {
        _exporterClass = exporterClass;
    }

    public boolean isPrettyPrint() {
        return _prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        _prettyPrint = prettyPrint;
    }

    /**
     * Export format.
     */
    public enum Format {
        YAML(YamlSinglePageExporter.class) {
            @Override
            public ContentHandler getContentHandler(OutputStream out, boolean prettyPrint) {
                // No xml namespaces allowed in YAML export
                NamespaceFilter filter = new NamespaceFilter();
                filter.setContentHandler(new YamlContentHandler(out));
                return filter;
            }
        };

        private final Class<? extends Exporter> _defaultExporter;

        Format(Class<? extends Exporter> defaultExporter) {
            _defaultExporter = defaultExporter;
        }

        public Class<? extends Exporter> getDefaultExporterClass() {
            return _defaultExporter;
        }

        protected ContentHandler getContentHandler(OutputStream out) {
            return getContentHandler(out, true);
        }

        protected abstract ContentHandler getContentHandler(OutputStream out, boolean prettyPrint);

        public static boolean isSupportedExtension(String extension) {
            return Arrays.stream(values()).anyMatch(f -> StringUtils.equals(f.name(), StringUtils.upperCase(extension)));
        }
    }

    /**
     * Exported file compression.
     */
    public enum Compression {
        NONE,
        ZIP,
        GZ
    }
}
