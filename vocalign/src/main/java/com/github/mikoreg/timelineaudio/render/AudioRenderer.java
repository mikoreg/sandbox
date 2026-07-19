package com.github.mikoreg.timelineaudio.render;

import com.github.mikoreg.timelineaudio.domain.RenderJob;
import com.github.mikoreg.timelineaudio.domain.RenderResult;

public interface AudioRenderer {
    RenderResult render(RenderJob job);
}
