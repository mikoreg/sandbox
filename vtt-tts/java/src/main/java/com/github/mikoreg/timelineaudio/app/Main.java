package com.github.mikoreg.timelineaudio.app;

import com.github.mikoreg.timelineaudio.domain.RenderJob;
import com.github.mikoreg.timelineaudio.domain.RenderResult;
import com.github.mikoreg.timelineaudio.domain.RenderSegment;
import com.github.mikoreg.timelineaudio.domain.SpeechSegment;
import com.github.mikoreg.timelineaudio.domain.SubtitleSegment;
import com.github.mikoreg.timelineaudio.domain.TimingDecision;
import com.github.mikoreg.timelineaudio.domain.TimingWindow;
import com.github.mikoreg.timelineaudio.render.AudioRenderer;
import com.github.mikoreg.timelineaudio.render.ffmpeg.FfmpegConcatRenderer;
import com.github.mikoreg.timelineaudio.render.mlt.MltXmlRenderer;
import com.github.mikoreg.timelineaudio.render.wav.WavTimelineRenderer;
import com.github.mikoreg.timelineaudio.speech.GttsHttpSpeechSynthesizer;
import com.github.mikoreg.timelineaudio.speech.MockSpeechSynthesizer;
import com.github.mikoreg.timelineaudio.speech.SpeechConfig;
import com.github.mikoreg.timelineaudio.speech.SpeechSynthesizer;
import com.github.mikoreg.timelineaudio.subtitles.SubtitleParser;
import com.github.mikoreg.timelineaudio.subtitles.TtmlSubtitleParser;
import com.github.mikoreg.timelineaudio.timing.TimingConfig;
import com.github.mikoreg.timelineaudio.timing.TimingPolicy;
import com.github.mikoreg.timelineaudio.timing.TimingWindowResolver;
import com.github.mikoreg.timelineaudio.timing.WindowedTimingPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final System.Logger LOG = System.getLogger(Main.class.getName());
    private static final Path DEFAULT_CONFIG_PATH = Path.of("config.properties");

    public static void main(String[] args) {
        AppConfig config = parseArgs(args);
        String ffmpegLogLevel = resolveFfmpegLogLevel(config);

        SubtitleParser parser = new TtmlSubtitleParser();
        List<SubtitleSegment> subtitles = parser.parse(config.inputPath());
        List<SubtitleSegment> previewed = applyPreview(subtitles, config.previewMs());

        SpeechConfig speechConfig = new SpeechConfig(
                config.language(),
                config.speechSpeed(),
                config.charactersPerSecond(),
                config.speechOutputDir()
        );
        SpeechSynthesizer synthesizer = createSpeechSynthesizer(config, speechConfig);

        TimingConfig timingConfig = new TimingConfig(
                config.minTrimMs(),
                config.speechSpeed(),
                config.maxSpeedFactor()
        );
        TimingPolicy timingPolicy = new WindowedTimingPolicy(timingConfig);
        TimingWindowResolver resolver = new TimingWindowResolver();
        AudioProbe probe = new AudioProbe();
        SegmentAudioProcessor processor = new SegmentAudioProcessor(probe, ffmpegLogLevel);

        List<PreparedSegment> prepared = new ArrayList<>();
        for (int i = 0; i < previewed.size(); i++) {
            SubtitleSegment subtitle = previewed.get(i);
            SpeechSegment rawSpeech = synthesizer.synthesize(subtitle);
            long rawDuration = probe.probeDurationMs(rawSpeech.audioPath());
            SpeechSegment speech = new SpeechSegment(rawSpeech.id(), rawSpeech.text(), rawSpeech.audioPath(), rawDuration);

            TimingWindow window = resolver.resolve(previewed, i);
            TimingDecision decision = timingPolicy.decide(subtitle, speech, window);
            SpeechSegment processed = processor.process(speech, decision, config.processedOutputDir());
            prepared.add(new PreparedSegment(subtitle, processed, decision));
        }

        BuildResult build = buildTimeline(prepared, config.allowStretchSegments(), config.stretchSegmentLimit());
        if (config.allowStretchSegments() && build.stretchedSegments() > config.stretchSegmentLimit()) {
            LOG.log(Level.WARNING, "Stretch limit exceeded (" + build.stretchedSegments() + " > "
                    + config.stretchSegmentLimit() + "); rebuilding without stretching.");
            build = buildTimeline(prepared, false, 0);
        }

        RenderJob job = new RenderJob(build.segments(), config.outputPath(), build.timelineMs(), config.frameRate());
        AudioRenderer renderer = createRenderer(config, ffmpegLogLevel);
        RenderResult result = renderer.render(job);

        LOG.log(Level.INFO, "Render complete: " + result.outputPath());
        LOG.log(Level.INFO, "Timeline duration: " + result.timelineDurationMs() + " ms");
        LOG.log(Level.INFO, "Segments rendered: " + result.segmentsRendered());

        if (config.encodeCodec() != null && config.renderer().equals("ffmpeg")) {
            FfmpegEncoder encoder = new FfmpegEncoder(ffmpegLogLevel);
            Path encoded = encoder.encode(result.outputPath(), config.encodeCodec());
            LOG.log(Level.INFO, "Encoded output: " + encoded);
        } else if (config.encodeCodec() != null) {
            LOG.log(Level.WARNING, "Encoding requested but renderer is not ffmpeg; skipping encoding.");
        }
    }

    private static SpeechSynthesizer createSpeechSynthesizer(AppConfig config, SpeechConfig speechConfig) {
        return switch (config.ttsProvider()) {
            case "gtts" -> new GttsHttpSpeechSynthesizer(speechConfig);
            case "mock" -> new MockSpeechSynthesizer(speechConfig);
            default -> throw new IllegalArgumentException("Unknown TTS provider: " + config.ttsProvider());
        };
    }

    private static AudioRenderer createRenderer(AppConfig config, String ffmpegLogLevel) {
        return switch (config.renderer()) {
            case "mlt" -> new MltXmlRenderer();
            case "wav" -> new WavTimelineRenderer();
            case "ffmpeg" -> new FfmpegConcatRenderer(ffmpegLogLevel);
            default -> throw new IllegalArgumentException("Unknown renderer: " + config.renderer());
        };
    }

    private static List<SubtitleSegment> applyPreview(List<SubtitleSegment> segments, long previewMs) {
        if (previewMs <= 0) {
            return segments;
        }
        List<SubtitleSegment> preview = new ArrayList<>();
        for (SubtitleSegment segment : segments) {
            if (segment.beginMs() >= previewMs) {
                break;
            }
            preview.add(segment);
        }
        LOG.log(Level.INFO, "Preview mode enabled, segments kept: " + preview.size());
        return preview;
    }

    private static AppConfig parseArgs(String[] args) {
        Path configPath = DEFAULT_CONFIG_PATH;
        String profile = null;
        ConfigOverrides overrides = new ConfigOverrides();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--config" -> configPath = Path.of(requireValue(args, ++i, "--config"));
                case "--profile" -> profile = requireValue(args, ++i, "--profile");
                case "--input" -> overrides.inputPath = Path.of(requireValue(args, ++i, "--input"));
                case "--output" -> overrides.setOutput(Path.of(requireValue(args, ++i, "--output")));
                case "--speech-output" -> overrides.speechOutputDir = Path.of(requireValue(args, ++i, "--speech-output"));
                case "--processed-output" -> overrides.processedOutputDir = Path.of(requireValue(args, ++i, "--processed-output"));
                case "--renderer" -> overrides.renderer = requireValue(args, ++i, "--renderer").toLowerCase();
                case "--encode" -> overrides.encodeCodec = requireValue(args, ++i, "--encode").toLowerCase();
                case "--tts-provider" -> overrides.ttsProvider = requireValue(args, ++i, "--tts-provider").toLowerCase();
                case "--language" -> overrides.language = requireValue(args, ++i, "--language");
                case "--ffmpeg-loglevel" -> overrides.ffmpegLogLevelOverride = requireValue(args, ++i, "--ffmpeg-loglevel").toLowerCase();
                case "--allow-stretch" -> overrides.allowStretchSegments = true;
                case "--no-stretch" -> overrides.allowStretchSegments = false;
                case "--stretch-limit" -> overrides.stretchSegmentLimit = Integer.parseInt(requireValue(args, ++i, "--stretch-limit"));
                case "--debug" -> overrides.debug = true;
                case "--preview-ms" -> overrides.previewMs = Long.parseLong(requireValue(args, ++i, "--preview-ms"));
                case "--diagnostic-seconds" -> overrides.previewMs = Long.parseLong(requireValue(args, ++i, "--diagnostic-seconds")) * 1000L;
                case "--speech-speed" -> overrides.speechSpeed = Double.parseDouble(requireValue(args, ++i, "--speech-speed"));
                case "--chars-per-second" -> overrides.charactersPerSecond = Double.parseDouble(requireValue(args, ++i, "--chars-per-second"));
                case "--min-trim-ms" -> overrides.minTrimMs = Long.parseLong(requireValue(args, ++i, "--min-trim-ms"));
                case "--max-speed-factor" -> overrides.maxSpeedFactor = Double.parseDouble(requireValue(args, ++i, "--max-speed-factor"));
                case "--frame-rate" -> overrides.frameRate = Double.parseDouble(requireValue(args, ++i, "--frame-rate"));
                case "--help" -> {
                    printUsageAndExit();
                }
                default -> {
                    if (arg.startsWith("--")) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
                }
            }
        }

        Properties properties = loadProperties(configPath);
        if (profile == null) {
            profile = properties.getProperty("profile");
        }

        Path inputPath = resolvePath(overrides.inputPath,
                property(properties, profile, "input"));
        if (inputPath == null) {
            printUsageAndExit();
        }

        Path outputPath = resolvePath(overrides.outputPath,
                property(properties, profile, "output"));
        Path speechOutputDir = resolvePath(overrides.speechOutputDir,
                property(properties, profile, "speechOutput"),
                Path.of("output", "tts"));
        Path processedOutputDir = resolvePath(overrides.processedOutputDir,
                property(properties, profile, "processedOutput"),
                Path.of("output", "processed"));

        String renderer = resolveValue(overrides.renderer,
                property(properties, profile, "renderer"),
                "ffmpeg");
        String encodeCodec = resolveValue(overrides.encodeCodec,
                property(properties, profile, "encode"),
                null);
        String ttsProvider = resolveValue(overrides.ttsProvider,
                property(properties, profile, "ttsProvider"),
                "gtts");
        String language = resolveValue(overrides.language,
                property(properties, profile, "language"),
                "pl");
        String ffmpegLogLevelOverride = resolveValue(overrides.ffmpegLogLevelOverride,
                property(properties, profile, "ffmpegLogLevel"),
                null);
        boolean debug = resolveBoolean(overrides.debug,
                property(properties, profile, "debug"),
                false);
        boolean allowStretchSegments = resolveBoolean(overrides.allowStretchSegments,
                property(properties, profile, "allowStretchSegments"),
                true);
        int stretchSegmentLimit = resolveInt(overrides.stretchSegmentLimit,
                property(properties, profile, "stretchSegmentLimit"),
                3);
        long previewMs = resolveLong(overrides.previewMs,
                property(properties, profile, "previewMs"),
                0L);
        double speechSpeed = resolveDouble(overrides.speechSpeed,
                property(properties, profile, "speechSpeed"),
                2.0);
        double charactersPerSecond = resolveDouble(overrides.charactersPerSecond,
                property(properties, profile, "charsPerSecond"),
                14.0);
        long minTrimMs = resolveLong(overrides.minTrimMs,
                property(properties, profile, "minTrimMs"),
                400L);
        double maxSpeedFactor = resolveDouble(overrides.maxSpeedFactor,
                property(properties, profile, "maxSpeedFactor"),
                3.0);
        double frameRate = resolveDouble(overrides.frameRate,
                property(properties, profile, "frameRate"),
                25.0);

        if (stretchSegmentLimit < 0) {
            throw new IllegalArgumentException("stretchSegmentLimit must be >= 0");
        }

        if (outputPath == null) {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            if (renderer.equals("mlt")) {
                outputPath = Path.of("output", "timeline_" + timestamp + ".mlt.xml");
            } else {
                outputPath = Path.of("output", "timeline_" + timestamp + ".wav");
            }
        }

        return new AppConfig(
                inputPath,
                outputPath,
                speechOutputDir,
                processedOutputDir,
                renderer,
                encodeCodec,
                ttsProvider,
                language,
                allowStretchSegments,
                stretchSegmentLimit,
                debug,
                previewMs,
                speechSpeed,
                charactersPerSecond,
                minTrimMs,
                maxSpeedFactor,
                frameRate,
                ffmpegLogLevelOverride
        );
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }

    private static void printUsageAndExit() {
        String usage = """
                Usage:
                  java -jar timeline-audio.jar --input <ttml-file> [options]

                Options:
                  --config <path>              Config properties file (default: config.properties)
                  --profile <name>             Properties profile (overrides config profile)
                  --output <path>              Output path (default: output/timeline.wav or output/timeline.mlt.xml)
                  --speech-output <dir>        Directory for raw TTS audio (default: output/tts)
                  --processed-output <dir>     Directory for processed WAV segments (default: output/processed)
                  --renderer <ffmpeg|wav|mlt>  Renderer selection (default: ffmpeg)
                  --encode <opus|mp3|aac>      Encode ffmpeg WAV output via ffmpeg
                  --tts-provider <gtts|mock>  TTS provider (default: gtts)
                  --language <code>           Language code for TTS (default: pl)
                  --ffmpeg-loglevel <level>   Override ffmpeg log level (trace|debug|info|warning|error|quiet)
                  --allow-stretch             Allow stretching segments when overlap occurs
                  --no-stretch                Disable stretching segments
                  --stretch-limit <count>     Max segments allowed to stretch (default: 3)
                  --debug                      Enable debug logs
                  --preview-ms <ms>            Limit processing to subtitles starting before this time
                  --diagnostic-seconds <s>     Shortcut for preview seconds
                  --speech-speed <float>       Base speech speed factor (default: 2.0)
                  --chars-per-second <float>   Mock speech base speed (default: 14.0)
                  --min-trim-ms <ms>           Minimum trim window (default: 400)
                  --max-speed-factor <f>       Max speed factor (default: 3.0)
                  --frame-rate <fps>           Frame rate for MLT output (default: 25)
                """;
        System.out.println(usage);
        System.exit(1);
    }

    private static String resolveFfmpegLogLevel(AppConfig config) {
        if (config.ffmpegLogLevelOverride() != null) {
            return config.ffmpegLogLevelOverride();
        }
        return System.getProperty("timelineaudio.ffmpeg.loglevel", "info");
    }

    private static BuildResult buildTimeline(List<PreparedSegment> prepared,
                                             boolean allowStretch,
                                             int stretchLimit) {
        List<RenderSegment> renderSegments = new ArrayList<>();
        long timelineMs = 0L;
        long cursorMs = 0L;
        int stretchedSegments = 0;

        for (PreparedSegment entry : prepared) {
            SubtitleSegment subtitle = entry.subtitle();
            SpeechSegment processed = entry.processed();
            long startMs = subtitle.beginMs();

            if (startMs < cursorMs) {
                if (allowStretch) {
                    stretchedSegments++;
                    startMs = cursorMs;
                    LOG.log(Level.WARNING, "Segment " + subtitle.id() + " overlaps timeline; stretching to "
                            + cursorMs + " ms (" + stretchedSegments + "/" + stretchLimit + ").");
                } else {
                    LOG.log(Level.WARNING, "Segment " + subtitle.id() + " overlaps timeline; stretching disabled.");
                }
            }

            renderSegments.add(new RenderSegment(subtitle.id(), processed.audioPath(), startMs,
                    processed.durationMs(), entry.decision().speedFactor()));

            long endMs = startMs + processed.durationMs();
            cursorMs = Math.max(cursorMs, endMs);
            timelineMs = Math.max(timelineMs, endMs);
            if (subtitle.endMs() != null) {
                timelineMs = Math.max(timelineMs, subtitle.endMs());
            }

            LOG.log(Level.DEBUG, "Segment " + subtitle.id() + " start=" + startMs + " ms duration="
                    + processed.durationMs() + " ms speed=" + entry.decision().speedFactor());
        }

        return new BuildResult(renderSegments, timelineMs, stretchedSegments);
    }

    private static Properties loadProperties(Path configPath) {
        if (configPath == null || !Files.exists(configPath)) {
            return new Properties();
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read config file " + configPath, e);
        }
        return properties;
    }

    private static String property(Properties properties, String profile, String key) {
        if (profile != null && !profile.isBlank()) {
            String value = properties.getProperty(profile + "." + key);
            if (value != null) {
                return value;
            }
        }
        return properties.getProperty(key);
    }

    private static Path resolvePath(Path override, String property) {
        if (override != null) {
            return override;
        }
        if (property != null && !property.isBlank()) {
            return Path.of(property.trim());
        }
        return null;
    }

    private static Path resolvePath(Path override, String property, Path defaultValue) {
        Path resolved = resolvePath(override, property);
        return resolved != null ? resolved : defaultValue;
    }

    private static String resolveValue(String override, String property, String defaultValue) {
        if (override != null) {
            return override;
        }
        if (property != null && !property.isBlank()) {
            return property.trim();
        }
        return defaultValue;
    }

    private static boolean resolveBoolean(Boolean override, String property, boolean defaultValue) {
        if (override != null) {
            return override;
        }
        if (property != null && !property.isBlank()) {
            return Boolean.parseBoolean(property.trim());
        }
        return defaultValue;
    }

    private static long resolveLong(Long override, String property, long defaultValue) {
        if (override != null) {
            return override;
        }
        if (property != null && !property.isBlank()) {
            return Long.parseLong(property.trim());
        }
        return defaultValue;
    }

    private static int resolveInt(Integer override, String property, int defaultValue) {
        if (override != null) {
            return override;
        }
        if (property != null && !property.isBlank()) {
            return Integer.parseInt(property.trim());
        }
        return defaultValue;
    }

    private static double resolveDouble(Double override, String property, double defaultValue) {
        if (override != null) {
            return override;
        }
        if (property != null && !property.isBlank()) {
            return Double.parseDouble(property.trim());
        }
        return defaultValue;
    }

    private record PreparedSegment(SubtitleSegment subtitle, SpeechSegment processed, TimingDecision decision) {
    }

    private record BuildResult(List<RenderSegment> segments, long timelineMs, int stretchedSegments) {
    }

    private static class ConfigOverrides {
        Path inputPath;
        Path outputPath;
        Path speechOutputDir;
        Path processedOutputDir;
        String renderer;
        String encodeCodec;
        String ttsProvider;
        String language;
        String ffmpegLogLevelOverride;
        Boolean allowStretchSegments;
        Integer stretchSegmentLimit;
        Boolean debug;
        Long previewMs;
        Double speechSpeed;
        Double charactersPerSecond;
        Long minTrimMs;
        Double maxSpeedFactor;
        Double frameRate;

        void setOutput(Path output) {
            this.outputPath = output;
        }
    }
}
