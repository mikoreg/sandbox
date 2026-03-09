package com.github.mikoreg.timelineaudio.timing;

import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;
import com.github.mikoreg.timelineaudio.domain.TimingDecision;
import com.github.mikoreg.timelineaudio.domain.TimingWindow;

public interface TimingPolicy {
    TimingDecision decide(SubtitleSegment subtitle, SpeechSegment speech, TimingWindow window);
}
