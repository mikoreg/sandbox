package com.github.mikoreg.timelineaudio.app;

import com.github.mikoreg.timelineaudio.domain.DebugLogger;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        AppConfig config = parseArgs(args);
        DebugLogger logger = new ConsoleDebugLogger(config.debug());

        SubtitleParser parser = new TtmlSubtitleParser(logger);
        List<SubtitleSegment> subtitles = parser.parse(config.inputPath());
        List<SubtitleSegment> previewed = applyPreview(subtitles, config.previewMs(), logger);

        SpeechConfig speechConfig = new SpeechConfig(
                config.language(),
                config.speechSpeed(),
                config.charactersPerSecond(),
                config.speechOutputDir()
        );
        SpeechSynthesizer synthesizer = createSpeechSynthesizer(config, speechConfig, logger);

        TimingConfig timingConfig = new TimingConfig(
                config.minTrimMs(),
                config.speechSpeed(),
                config.maxSpeedFactor()
        );
        TimingPolicy timingPolicy = new WindowedTimingPolicy(timingConfig, logger);
        TimingWindowResolver resolver = new TimingWindowResolver();
        AudioProbe probe = new AudioProbe(logger);
        SegmentAudioProcessor processor = new SegmentAudioProcessor(logger, probe);

        List<RenderSegment> renderSegments = new ArrayList<>();
        long timelineMs = 0L;
        long cursorMs = 0L;
        for (int i = 0; i < previewed.size(); i++) {
            SubtitleSegment subtitle = previewed.get(i);
            SpeechSegment rawSpeech = synthesizer.synthesize(subtitle);
            long rawDuration = probe.probeDurationMs(rawSpeech.audioPath());
            SpeechSegment speech = new SpeechSegment(rawSpeech.id(), rawSpeech.text(), rawSpeech.audioPath(), rawDuration);

            TimingWindow window = resolver.resolve(previewed, i);
            TimingDecision decision = timingPolicy.decide(subtitle, speech, window);
            SpeechSegment processed = processor.process(speech, decision, config.processedOutputDir());

            long startMs = resolveStart(subtitle.beginMs(), cursorMs, config.allowPushSegments(), logger, subtitle.id());
            renderSegments.add(new RenderSegment(subtitle.id(), processed.audioPath(), startMs, processed.durationMs(), decision.speedFactor()));

            long endMs = startMs + processed.durationMs();
            cursorMs = Math.max(cursorMs, endMs);
            timelineMs = Math.max(timelineMs, endMs);
            if (subtitle.endMs() != null) {
                timelineMs = Math.max(timelineMs, subtitle.endMs());
            }

            logger.debug("Segment " + subtitle.id() + " start=" + startMs + " ms duration=" + processed.durationMs()
                    + " ms speed=" + decision.speedFactor());
        }

        RenderJob job = new RenderJob(renderSegments, config.outputPath(), timelineMs, config.frameRate());
        AudioRenderer renderer = createRenderer(config, logger);
        RenderResult result = renderer.render(job);

        logger.info("Render complete: " + result.outputPath());
        logger.info("Timeline duration: " + result.timelineDurationMs() + " ms");
        logger.info("Segments rendered: " + result.segmentsRendered());

        if (config.encodeCodec() != null && config.renderer().equals("ffmpeg")) {
            FfmpegEncoder encoder = new FfmpegEncoder(logger);
            Path encoded = encoder.encode(result.outputPath(), config.encodeCodec());
            logger.info("Encoded output: " + encoded);
        } else if (config.encodeCodec() != null) {
            logger.warn("Encoding requested but renderer is not ffmpeg; skipping encoding.");
        }
    }

    private static SpeechSynthesizer createSpeechSynthesizer(AppConfig config, SpeechConfig speechConfig, DebugLogger logger) {
        return switch (config.ttsProvider()) {
            case "gtts" -> new GttsHttpSpeechSynthesizer(speechConfig, logger);
            case "mock" -> new MockSpeechSynthesizer(speechConfig, logger);
            default -> throw new IllegalArgumentException("Unknown TTS provider: " + config.ttsProvider());
        };
    }

    private static AudioRenderer createRenderer(AppConfig config, DebugLogger logger) {
        return switch (config.renderer()) {
            case "mlt" -> new MltXmlRenderer(logger);
            case "wav" -> new WavTimelineRenderer(logger);
            case "ffmpeg" -> new FfmpegConcatRenderer(logger);
            default -> throw new IllegalArgumentException("Unknown renderer: " + config.renderer());
        };
    }

    private static long resolveStart(long beginMs, long cursorMs, boolean allowPush, DebugLogger logger, String id) {
        if (beginMs >= cursorMs) {
            return beginMs;
        }
        if (allowPush) {
            logger.warn("Segment " + id + " overlaps timeline; pushing to " + cursorMs + " ms.");
        } else {
            logger.warn("Segment " + id + " overlaps timeline; renderer uses concat so pushing to " + cursorMs + " ms.");
        }
        return cursorMs;
    }

    private static List<SubtitleSegment> applyPreview(List<SubtitleSegment> segments, long previewMs, DebugLogger logger) {
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
        logger.info("Preview mode enabled, segments kept: " + preview.size());
        return preview;
    }

    private static AppConfig parseArgs(String[] args) {
        Path inputPath = null;
        Path outputPath = Path.of("output", "timeline.wav");
        Path speechOutputDir = Path.of("output", "tts");
        Path processedOutputDir = Path.of("output", "processed");
        String renderer = "ffmpeg";
        String encodeCodec = null;
        String ttsProvider = "gtts";
        String language = "pl";
        boolean outputProvided = false;
        boolean debug = false;
        boolean allowPushSegments = false;
        long previewMs = 0L;
        double speechSpeed = 2.0;
        double charactersPerSecond = 14.0;
        long minTrimMs = 400L;
        double maxSpeedFactor = 3.0;
        double frameRate = 25.0;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--input" -> inputPath = Path.of(requireValue(args, ++i, "--input"));
                case "--output" -> {
                    outputPath = Path.of(requireValue(args, ++i, "--output"));
                    outputProvided = true;
                }
                case "--speech-output" -> speechOutputDir = Path.of(requireValue(args, ++i, "--speech-output"));
                case "--processed-output" -> processedOutputDir = Path.of(requireValue(args, ++i, "--processed-output"));
                case "--renderer" -> renderer = requireValue(args, ++i, "--renderer").toLowerCase();
                case "--encode" -> encodeCodec = requireValue(args, ++i, "--encode").toLowerCase();
                case "--tts-provider" -> ttsProvider = requireValue(args, ++i, "--tts-provider").toLowerCase();
                case "--language" -> language = requireValue(args, ++i, "--language");
                case "--debug" -> debug = true;
                case "--allow-push" -> allowPushSegments = true;
                case "--preview-ms" -> previewMs = Long.parseLong(requireValue(args, ++i, "--preview-ms"));
                case "--diagnostic-seconds" -> previewMs = Long.parseLong(requireValue(args, ++i, "--diagnostic-seconds")) * 1000L;
                case "--speech-speed" -> speechSpeed = Double.parseDouble(requireValue(args, ++i, "--speech-speed"));
                case "--chars-per-second" -> charactersPerSecond = Double.parseDouble(requireValue(args, ++i, "--chars-per-second"));
                case "--min-trim-ms" -> minTrimMs = Long.parseLong(requireValue(args, ++i, "--min-trim-ms"));
                case "--max-speed-factor" -> maxSpeedFactor = Double.parseDouble(requireValue(args, ++i, "--max-speed-factor"));
                case "--frame-rate" -> frameRate = Double.parseDouble(requireValue(args, ++i, "--frame-rate"));
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

        if (inputPath == null) {
            printUsageAndExit();
        }

        if (!outputProvided) {
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
                allowPushSegments,
                debug,
                previewMs,
                speechSpeed,
                charactersPerSecond,
                minTrimMs,
                maxSpeedFactor,
                frameRate
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
                  --output <path>              Output path (default: output/timeline.wav or output/timeline.mlt.xml)
                  --speech-output <dir>        Directory for raw TTS audio (default: output/tts)
                  --processed-output <dir>     Directory for processed WAV segments (default: output/processed)
                  --renderer <ffmpeg|wav|mlt>  Renderer selection (default: ffmpeg)
                  --encode <opus|mp3|aac>      Encode ffmpeg WAV output via ffmpeg
                  --tts-provider <gtts|mock>  TTS provider (default: gtts)
                  --language <code>           Language code for TTS (default: pl)
                  --allow-push                Allow pushing segments when overlap occurs
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
}
