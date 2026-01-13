package com.github.mikoreg.timelineaudio.timing;

import com.github.mikoreg.timelineaudio.domain.NoOpDebugLogger;
import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;
import com.github.mikoreg.timelineaudio.domain.TimingDecision;
import com.github.mikoreg.timelineaudio.domain.TimingWindow;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowedTimingPolicyTest {
    @Test
    void allowsOverflowWhenWindowTooSmall() {
        TimingConfig config = new TimingConfig(400, 2.0, 3.0);
        WindowedTimingPolicy policy = new WindowedTimingPolicy(config, new NoOpDebugLogger());
        SubtitleSegment subtitle = new SubtitleSegment("seg-1", "Hello", 0, 300L);
        SpeechSegment speech = new SpeechSegment("seg-1", "Hello", Path.of("mock.wav"), 1000L);
        TimingWindow window = new TimingWindow(0, 300L);

        TimingDecision decision = policy.decide(subtitle, speech, window);

        assertTrue(decision.allowOverflow());
        assertEquals(2.0, decision.speedFactor());
    }
}
