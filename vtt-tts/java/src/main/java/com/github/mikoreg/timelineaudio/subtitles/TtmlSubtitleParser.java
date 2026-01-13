package com.github.mikoreg.timelineaudio.subtitles;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TtmlSubtitleParser implements SubtitleParser {
    public static final String TTML_NAMESPACE = "http://www.w3.org/ns/ttml";

    private final DebugLogger logger;

    public TtmlSubtitleParser(DebugLogger logger) {
        this.logger = logger;
    }

    @Override
    public List<SubtitleSegment> parse(Path inputPath) {
        logger.info("Parsing TTML file: " + inputPath);
        List<SubtitleSegment> segments = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document document = factory.newDocumentBuilder().parse(inputPath.toFile());
            NodeList nodes = document.getElementsByTagNameNS(TTML_NAMESPACE, "p");
            if (nodes.getLength() == 0) {
                logger.warn("No TTML namespace nodes found, falling back to <p> lookup without namespace.");
                nodes = document.getElementsByTagName("p");
            }

            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String text = element.getTextContent();
                if (text == null || text.isBlank()) {
                    continue;
                }
                text = text.trim();
                String beginAttr = element.getAttribute("begin");
                String endAttr = element.getAttribute("end");
                if (beginAttr == null || beginAttr.isBlank()) {
                    logger.debug("Skipping segment without begin time at index " + i);
                    continue;
                }
                long beginMs = TtmlTimecodeParser.parseToMs(beginAttr.trim());
                Long endMs = null;
                if (endAttr != null && !endAttr.isBlank()) {
                    endMs = TtmlTimecodeParser.parseToMs(endAttr.trim());
                }
                String id = "seg-" + (segments.size() + 1);
                segments.add(new SubtitleSegment(id, text, beginMs, endMs));
            }

            logger.info("Parsed segments: " + segments.size());
            return segments;
        } catch (Exception e) {
            logger.error("Failed to parse TTML file", e);
            throw new IllegalStateException("TTML parsing failed", e);
        }
    }
}
