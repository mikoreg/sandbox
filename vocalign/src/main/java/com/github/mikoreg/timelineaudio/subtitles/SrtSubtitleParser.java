package com.github.mikoreg.timelineaudio.subtitles;

import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SrtSubtitleParser implements SubtitleParser {
    private static final System.Logger LOG = System.getLogger(SrtSubtitleParser.class.getName());

    @Override
    public List<SubtitleSegment> parse(Path inputPath) {
        LOG.log(Level.INFO, "Parsing SRT file: " + inputPath);
        List<String> lines;
        try {
            lines = Files.readAllLines(inputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SRT file " + inputPath, e);
        }

        List<SubtitleSegment> segments = new ArrayList<>();
        int index = 0;
        int segmentIndex = 1;
        while (index < lines.size()) {
            String line = lines.get(index).trim();
            if (line.isEmpty()) {
                index++;
                continue;
            }
            if (isNumeric(line)) {
                index++;
                if (index >= lines.size()) {
                    break;
                }
                line = lines.get(index).trim();
            }
            if (!line.contains("-->")) {
                index++;
                continue;
            }

            String[] parts = line.split("-->");
            if (parts.length < 2) {
                index++;
                continue;
            }
            long startMs = parseTime(parts[0].trim());
            long endMs = parseTime(parts[1].trim());
            index++;

            StringBuilder text = new StringBuilder();
            while (index < lines.size()) {
                String textLine = lines.get(index);
                if (textLine.trim().isEmpty()) {
                    break;
                }
                if (!text.isEmpty()) {
                    text.append('\n');
                }
                text.append(textLine.trim());
                index++;
            }

            if (!text.isEmpty()) {
                segments.add(new SubtitleSegment("seg-" + segmentIndex, text.toString(), startMs, endMs));
                segmentIndex++;
            }

            index++;
        }

        LOG.log(Level.INFO, "Parsed segments: " + segments.size());
        return segments;
    }

    private boolean isNumeric(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (!Character.isDigit(line.charAt(i))) {
                return false;
            }
        }
        return !line.isEmpty();
    }

    private long parseTime(String value) {
        String[] parts = value.split(":");
        if (parts.length != 3 && parts.length != 2) {
            throw new IllegalArgumentException("Invalid SRT time: " + value);
        }
        int hours = 0;
        int minutes;
        String secondsPart;
        if (parts.length == 3) {
            hours = Integer.parseInt(parts[0]);
            minutes = Integer.parseInt(parts[1]);
            secondsPart = parts[2];
        } else {
            minutes = Integer.parseInt(parts[0]);
            secondsPart = parts[1];
        }
        String[] secParts = secondsPart.split(",");
        if (secParts.length != 2) {
            throw new IllegalArgumentException("Invalid SRT time: " + value);
        }
        int seconds = Integer.parseInt(secParts[0]);
        int millis = Integer.parseInt(secParts[1]);
        return hours * 3600000L + minutes * 60000L + seconds * 1000L + millis;
    }
}
