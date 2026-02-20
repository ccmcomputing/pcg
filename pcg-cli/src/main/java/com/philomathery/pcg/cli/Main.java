package com.philomathery.pcg.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.xmlgraphics.util.MimeConstants;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "pcg", mixinStandardHelpOptions = true, version = "0.1.0",
        description = "Render certificates to PDF using Apache FOP")
public class Main implements Runnable {

    @Option(names = "--xml", description = "Path to XML data file")
    private File xmlFile;

    @Option(names = "--json", description = "Path to JSON data file")
    private File jsonFile;

    @Option(names = "--xslt", description = "Path to XSLT stylesheet", required = true)
    private File xsltFile;

    @Option(names = "--out", description = "Path to output PDF file, or '-' for stdout", required = true)
    private String out;

    @Option(names = "--conf", description = "Path to FOP configuration (fop.xconf)")
    private File fopConf;

    @Option(names = "--dump-fo", description = "Write intermediate XSL-FO to this path for debugging")
    private File dumpFo;

    @Override
    public void run() {
        try {
            if (xmlFile == null && jsonFile == null) {
                throw new IllegalArgumentException("Must provide either --xml or --json");
            }
            if (xmlFile != null && xmlFile.length() == 0) {
                throw new IllegalArgumentException("XML input is empty: " + xmlFile.getAbsolutePath());
            }
            if (jsonFile != null && jsonFile.length() == 0) {
                throw new IllegalArgumentException("JSON input is empty: " + jsonFile.getAbsolutePath());
            }
            if (xsltFile.length() == 0) {
                throw new IllegalArgumentException("XSLT input is empty: " + xsltFile.getAbsolutePath());
            }
            FopFactory fopFactory = buildFopFactory();
            try (InputStream xsltIn = new FileInputStream(xsltFile);
                 OutputStream outStream = openOut(out)) {

                FOUserAgent userAgent = fopFactory.newFOUserAgent();
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, outStream);

                TransformerFactory tf = TransformerFactory.newInstance();
                Templates templates = tf.newTemplates(new StreamSource(xsltIn));
                Transformer transformer = templates.newTransformer();

                Source source = getSource();
                if (dumpFo != null) {
                    writeFoAndRender(transformer, source, fop);
                } else {
                    Result result = new SAXResult(fop.getDefaultHandler());
                    transformer.transform(source, result);
                }
            }
        } catch (Exception e) {
            System.err.println("Render failed: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(4);
        }
    }

    private Source getSource() throws Exception {
        if (xmlFile != null) {
            return new StreamSource(new FileInputStream(xmlFile));
        }
        
        // Process JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonFile);
        if (!root.isArray()) {
            throw new IllegalArgumentException("JSON root must be an array of certificates");
        }
        
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<certificates>");
        for (JsonNode node : root) {
            xmlBuilder.append("<certificate>");
            xmlBuilder.append(jsonToXml(node));
            xmlBuilder.append("</certificate>");
        }
        xmlBuilder.append("</certificates>");
        
        return new StreamSource(new java.io.ByteArrayInputStream(xmlBuilder.toString().getBytes("UTF-8")));
    }
    
    private String jsonToXml(JsonNode node) {
        StringBuilder sb = new StringBuilder();
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                sb.append("<").append(entry.getKey()).append(">");
                sb.append(jsonToXml(entry.getValue()));
                sb.append("</").append(entry.getKey()).append(">");
            });
        } else if (node.isValueNode()) {
            sb.append(escapeXml(node.asText()));
        }
        return sb.toString();
    }
    
    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }


    private FopFactory buildFopFactory() throws Exception {
        if (fopConf != null) {
            FopConfParser parser = new FopConfParser(fopConf);
            FopFactoryBuilder builder = parser.getFopFactoryBuilder();
            return builder.build();
        }
        // default factory without external config
        return FopFactory.newInstance(new File(".").toURI());
    }

    private static OutputStream openOut(String out) throws Exception {
        if ("-".equals(out)) {
            return System.out;
        }
        Path p = Paths.get(out).toAbsolutePath();
        Files.createDirectories(p.getParent());
        return Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private void writeFoAndRender(Transformer transformer, Source source, Fop fop) throws Exception {
        Path foPath = dumpFo.toPath().toAbsolutePath();
        Files.createDirectories(foPath.getParent());
        try (OutputStream foOut = Files.newOutputStream(foPath, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            transformer.transform(source, new StreamResult(foOut));
        }
        try (InputStream foIn = new FileInputStream(foPath.toFile())) {
            Transformer identity = TransformerFactory.newInstance().newTransformer();
            identity.transform(new StreamSource(foIn), new SAXResult(fop.getDefaultHandler()));
        }
    }

    public static void main(String[] args) {
        int exit = new CommandLine(new Main()).execute(args);
        System.exit(exit);
    }
}


