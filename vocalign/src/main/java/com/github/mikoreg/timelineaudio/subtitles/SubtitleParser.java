package com.github.mikoreg.timelineaudio.subtitles;

import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;

import java.nio.file.Path;
import java.util.List;

public interface SubtitleParser {
    List<SubtitleSegment> parse(Path inputPath);
}
