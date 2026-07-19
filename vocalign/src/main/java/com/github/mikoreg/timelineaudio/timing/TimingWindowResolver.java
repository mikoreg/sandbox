package com.github.mikoreg.timelineaudio.timing;

import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;
import com.github.mikoreg.timelineaudio.domain.TimingWindow;

import java.util.List;

public class TimingWindowResolver {
    public TimingWindow resolve(List<SubtitleSegment> segments, int index) {
        SubtitleSegment current = segments.get(index);
        Long end = current.endMs();
        if (end == null && index + 1 < segments.size()) {
            SubtitleSegment next = segments.get(index + 1);
            end = next.beginMs();
        }
        return new TimingWindow(current.beginMs(), end);
    }
}
