package com.github.mikoreg.timelineaudio.speech;

import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;

public interface SpeechSynthesizer {
    SpeechSegment synthesize(SubtitleSegment subtitle);
}
