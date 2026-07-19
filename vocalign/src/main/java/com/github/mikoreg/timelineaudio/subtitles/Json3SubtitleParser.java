package com.github.mikoreg.timelineaudio.subtitles;

import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Json3SubtitleParser implements SubtitleParser {
    private static final System.Logger LOG = System.getLogger(Json3SubtitleParser.class.getName());

    @Override
    public List<SubtitleSegment> parse(Path inputPath) {
        LOG.log(Level.INFO, "Parsing JSON3 file: " + inputPath);
        String content;
        try {
            content = Files.readString(inputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read JSON file " + inputPath, e);
        }

        Object root = new JsonParser(content).parse();
        if (!(root instanceof Map)) {
            throw new IllegalStateException("Unexpected JSON root structure in " + inputPath);
        }
        Object eventsValue = ((Map<?, ?>) root).get("events");
        if (!(eventsValue instanceof List)) {
            throw new IllegalStateException("Missing events array in " + inputPath);
        }

        List<SubtitleSegment> segments = new ArrayList<>();
        int segmentIndex = 1;
        for (Object entry : (List<?>) eventsValue) {
            if (!(entry instanceof Map)) {
                continue;
            }
            Map<?, ?> event = (Map<?, ?>) entry;
            Long startMs = toLong(event.get("tStartMs"));
            if (startMs == null) {
                continue;
            }
            Long durationMs = toLong(event.get("dDurationMs"));
            Long endMs = durationMs != null ? startMs + durationMs : null;
            boolean append = toBoolean(event.get("aAppend"));

            Object segsValue = event.get("segs");
            if (!(segsValue instanceof List)) {
                continue;
            }
            String text = buildText((List<?>) segsValue);
            if (text.isEmpty() && !append) {
                continue;
            }

            if (append && !segments.isEmpty()) {
                SubtitleSegment last = segments.remove(segments.size() - 1);
                String merged = mergeText(last.text(), text);
                Long mergedEnd = last.endMs();
                if (endMs != null) {
                    mergedEnd = mergedEnd == null ? endMs : Math.max(mergedEnd, endMs);
                }
                segments.add(new SubtitleSegment(last.id(), merged, last.beginMs(), mergedEnd));
            } else {
                segments.add(new SubtitleSegment("seg-" + segmentIndex, text, startMs, endMs));
                segmentIndex++;
            }
        }

        LOG.log(Level.INFO, "Parsed segments: " + segments.size());
        return segments;
    }

    private String buildText(List<?> segs) {
        StringBuilder builder = new StringBuilder();
        for (Object seg : segs) {
            if (!(seg instanceof Map)) {
                continue;
            }
            Object textValue = ((Map<?, ?>) seg).get("utf8");
            if (!(textValue instanceof String)) {
                continue;
            }
            String part = (String) textValue;
            if (part.equals("\n")) {
                builder.append('\n');
                continue;
            }
            if (builder.length() > 0 && !Character.isWhitespace(builder.charAt(builder.length() - 1))
                    && !part.startsWith(" ")) {
                builder.append(' ');
            }
            builder.append(part.trim());
        }
        return builder.toString().trim();
    }

    private String mergeText(String base, String append) {
        if (append.isEmpty()) {
            return base;
        }
        if (base.isEmpty()) {
            return append;
        }
        if (append.startsWith("\n") || base.endsWith("\n")) {
            return base + append;
        }
        return base + " " + append;
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            return Long.parseLong(((String) value).trim());
        }
        return null;
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean(((String) value).trim());
        }
        return false;
    }

    private static class JsonParser {
        private final String input;
        private int pos;

        JsonParser(String input) {
            this.input = input;
        }

        Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= input.length()) {
                throw new IllegalStateException("Unexpected end of JSON");
            }
            char ch = input.charAt(pos);
            if (ch == '{') {
                return parseObject();
            }
            if (ch == '[') {
                return parseArray();
            }
            if (ch == '"') {
                return parseString();
            }
            if (ch == 't' || ch == 'f') {
                return parseBoolean();
            }
            if (ch == 'n') {
                return parseNull();
            }
            if (ch == '-' || Character.isDigit(ch)) {
                return parseNumber();
            }
            throw new IllegalStateException("Unexpected JSON token at " + pos);
        }

        private Map<String, Object> parseObject() {
            expect('{');
            skipWhitespace();
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            if (peek('}')) {
                pos++;
                return map;
            }
            while (pos < input.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek(',')) {
                    pos++;
                    continue;
                }
                if (peek('}')) {
                    pos++;
                    break;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            expect('[');
            skipWhitespace();
            List<Object> list = new ArrayList<>();
            if (peek(']')) {
                pos++;
                return list;
            }
            while (pos < input.length()) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (peek(',')) {
                    pos++;
                    continue;
                }
                if (peek(']')) {
                    pos++;
                    break;
                }
            }
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (pos < input.length()) {
                char ch = input.charAt(pos++);
                if (ch == '"') {
                    break;
                }
                if (ch == '\\') {
                    if (pos >= input.length()) {
                        break;
                    }
                    char esc = input.charAt(pos++);
                    switch (esc) {
                        case '"', '\\', '/' -> builder.append(esc);
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' -> builder.append(parseUnicode());
                        default -> builder.append(esc);
                    }
                } else {
                    builder.append(ch);
                }
            }
            return builder.toString();
        }

        private char parseUnicode() {
            if (pos + 4 > input.length()) {
                return '?';
            }
            String hex = input.substring(pos, pos + 4);
            pos += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object parseNumber() {
            int start = pos;
            while (pos < input.length()) {
                char ch = input.charAt(pos);
                if (!Character.isDigit(ch) && ch != '-' && ch != '.' && ch != 'e' && ch != 'E' && ch != '+') {
                    break;
                }
                pos++;
            }
            String value = input.substring(start, pos);
            if (value.contains(".") || value.contains("e") || value.contains("E")) {
                return Double.parseDouble(value);
            }
            return Long.parseLong(value);
        }

        private Boolean parseBoolean() {
            if (input.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (input.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            throw new IllegalStateException("Invalid boolean at " + pos);
        }

        private Object parseNull() {
            if (input.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            throw new IllegalStateException("Invalid null at " + pos);
        }

        private void skipWhitespace() {
            while (pos < input.length()) {
                char ch = input.charAt(pos);
                if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                    pos++;
                } else {
                    break;
                }
            }
        }

        private void expect(char ch) {
            if (pos >= input.length() || input.charAt(pos) != ch) {
                throw new IllegalStateException("Expected '" + ch + "' at " + pos);
            }
            pos++;
        }

        private boolean peek(char ch) {
            return pos < input.length() && input.charAt(pos) == ch;
        }
    }
}
