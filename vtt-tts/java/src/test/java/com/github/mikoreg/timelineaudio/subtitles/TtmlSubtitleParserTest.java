package com.github.mikoreg.timelineaudio.subtitles;

import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TtmlSubtitleParserTest {
    @Test
    void parsesSegmentsFromTtml() throws Exception {
        String xml = """
                <?xml version=\"1.0\" encoding=\"UTF-8\"?>
                <tt xmlns=\"http://www.w3.org/ns/ttml\">
                  <body>
                    <div>
                      <p begin=\"00:00:01.500\" end=\"00:00:03.000\">Hello</p>
                      <p begin=\"00:00:04\">World</p>
                    </div>
                  </body>
                </tt>
                """;
        Path temp = Files.createTempFile("sample", ".ttml");
        Files.writeString(temp, xml);

        TtmlSubtitleParser parser = new TtmlSubtitleParser();
        List<SubtitleSegment> segments = parser.parse(temp);

        assertEquals(2, segments.size());
        SubtitleSegment first = segments.get(0);
        assertEquals(1500L, first.beginMs());
        assertEquals(3000L, first.endMs());
        assertNotNull(first.text());
    }
}
