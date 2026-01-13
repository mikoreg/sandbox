package com.github.mikoreg.timelineaudio.speech;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GttsHttpSpeechSynthesizer implements SpeechSynthesizer {
    private final SpeechConfig config;
    private final DebugLogger logger;
    private final HttpClient httpClient;

    public GttsHttpSpeechSynthesizer(SpeechConfig config, DebugLogger logger) {
        this.config = config;
        this.logger = logger;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public SpeechSegment synthesize(SubtitleSegment subtitle) {
        String text = subtitle.text();
        Path outputPath = config.outputDirectory().resolve(subtitle.id() + ".mp3");
        FilesUtil.ensureDirectory(config.outputDirectory());

        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl="
                + config.language() + "&q=" + encoded;

        logger.debug("Requesting gTTS for " + subtitle.id());
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("gTTS HTTP status " + response.statusCode());
            }
            Files.write(outputPath, response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("gTTS request failed", e);
        }

        return new SpeechSegment(subtitle.id(), text, outputPath, 0L);
    }

    private static class FilesUtil {
        static void ensureDirectory(Path path) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create directory " + path, e);
            }
        }
    }
}
