import xml.etree.ElementTree as ET
import os
import io
import datetime
try:
    from gtts import gTTS
except ModuleNotFoundError as e:
    missing = getattr(e, 'name', str(e))
    print(f"Missing dependency: {missing}. Install with: py -m pip install gTTS")
    raise

# Diagnostic-only mode using only gTTS (no pydub). Set env DIAG_GTTSSAMPLE=1 to enable.
DIAG_GTTSSAMPLE = os.environ.get("DIAG_GTTSSAMPLE", "0") == "1"

if not DIAG_GTTSSAMPLE:
    try:
        from pydub import AudioSegment
        USE_PYDUB = True
    except ModuleNotFoundError as e:
        missing = getattr(e, 'name', str(e))
        print(f"pydub not available ({missing}), falling back to ffmpeg-only concatenation.")
        print("Install pydub and ensure stdlib 'audioop' present for full pydub support: py -m pip install pydub")
        USE_PYDUB = False

# pydub relies on the stdlib `audioop` module; if it's missing (e.g. on some builds/Python 3.14),
# suggest using Python 3.11/3.10 or installing a compatible substitute.
try:
    import audioop
except ModuleNotFoundError:
    print("Warning: stdlib 'audioop' module not found. If you see related errors, consider using Python 3.11 or installing a compatible 'pyaudioop' for your Python version.")

import io
import datetime
import os

# Optional preview duration (milliseconds). Set PREVIEW_MS env var to limit generated audio length for diagnostics.
try:
    PREVIEW_MS = int(os.environ.get("PREVIEW_MS", "0"))
except ValueError:
    PREVIEW_MS = 0

# Speech speed multiplier. Base speed is 2.0 (double speed). SPEECH_SPEED can be set to any value >= 2.0 to further accelerate playback.
try:
    SPEECH_SPEED = float(os.environ.get("SPEECH_SPEED", "2.0"))
except ValueError:
    SPEECH_SPEED = 2.0

if SPEECH_SPEED < 2.0:
    print(f"Invalid or too-small SPEECH_SPEED={SPEECH_SPEED}; minimum base speed is 2.0 — clamping to 2.0")
    SPEECH_SPEED = 2.0

if not DIAG_GTTSSAMPLE:
    # Auto-detect ffmpeg path (user's local install). Update this path if your ffmpeg is located elsewhere.
    _ffmpeg_candidate = r"C:\Programy\ffmpeg-6.0-full_build\bin\ffmpeg.exe"
    if os.path.exists(_ffmpeg_candidate) and 'USE_PYDUB' in globals() and USE_PYDUB:
        AudioSegment.converter = _ffmpeg_candidate

# Configuration
INPUT_FILENAME = "MFG-S03E02.pl.ttml.xml"
OUTPUT_FILENAME = "generated_audio.mp3"
TTS_LANGUAGE = 'pl'  # Text is in Polish, so TTS must read in Polish

def parse_ttml_timestamp(time_str):
    """
    Converts TTML timestamp format (HH:MM:SS.mmm) to milliseconds.
    """
    try:
        # Format example: 00:00:06.250
        dt = datetime.datetime.strptime(time_str, "%H:%M:%S.%f")
        delta = datetime.timedelta(
            hours=dt.hour, 
            minutes=dt.minute, 
            seconds=dt.second, 
            microseconds=dt.microsecond
        )
        return int(delta.total_seconds() * 1000)
    except ValueError:
        # Fallback for format without milliseconds: 00:00:06
        dt = datetime.datetime.strptime(time_str, "%H:%M:%S")
        delta = datetime.timedelta(
            hours=dt.hour, 
            minutes=dt.minute, 
            seconds=dt.second
        )
        return int(delta.total_seconds() * 1000)

def generate_audio_from_subtitles(input_path, output_path):
    if not os.path.exists(input_path):
        print(f"Error: Input file '{input_path}' not found.")
        return

    print(f"Parsing XML file: {input_path}...")
    
    # Parse XML
    try:
        tree = ET.parse(input_path)
        root = tree.getroot()
    except ET.ParseError as e:
        print(f"Error parsing XML: {e}")
        return

    # XML Namespaces usually found in TTML
    namespaces = {'tt': 'http://www.w3.org/ns/ttml'}
    
    # Initialize empty audio track
    full_audio_track = AudioSegment.silent(duration=0)
    current_track_position_ms = 0
    
    # Find all paragraph elements <p>
    paragraphs = root.findall('.//tt:p', namespaces)
    total_lines = len(paragraphs)
    
    print(f"Found {total_lines} subtitle lines. Starting audio generation...")

    for index, p_element in enumerate(paragraphs):
        text_content = p_element.text
        
        # Skip empty lines
        if not text_content or not text_content.strip():
            continue
            
        text_content = text_content.strip()
        begin_time_str = p_element.get('begin')
        
        if not begin_time_str:
            continue

        target_start_ms = parse_ttml_timestamp(begin_time_str)
        
        # Calculate duration of silence needed to reach the target start time
        silence_duration = target_start_ms - current_track_position_ms
        
        if silence_duration > 0:
            # Add silence if we are ahead of the target time
            full_audio_track += AudioSegment.silent(duration=silence_duration)
            current_track_position_ms += silence_duration
        else:
            # If negative, previous audio overlapped. Try to trim the previous audio
            overlap = -silence_duration
            if len(full_audio_track) > overlap:
                full_audio_track = full_audio_track[:-overlap]
                current_track_position_ms -= overlap
                print(f"Adjusted previous audio by trimming {overlap} ms to avoid overlap.")
            else:
                # If overlap is larger than the whole track, reset to silent
                full_audio_track = AudioSegment.silent(duration=0)
                current_track_position_ms = 0
                print(f"Previous audio fully trimmed ({overlap} ms); starting fresh at {target_start_ms} ms.")

        # Note: after this, target_start_ms should be >= current_track_position_ms (or close)

        print(f"[{index + 1}/{total_lines}] Processing: {text_content[:40]}...")

        # Generate Speech using Google TTS
        # We use a BytesIO buffer to avoid saving temp files to disk
        try:
            tts = gTTS(text=text_content, lang=TTS_LANGUAGE)
            audio_buffer = io.BytesIO()
            tts.write_to_fp(audio_buffer)
            audio_buffer.seek(0)
            
            # Convert MP3 buffer to AudioSegment
            segment = AudioSegment.from_file(audio_buffer, format="mp3")

            # Apply global speed change if requested (pydub-based)
            if SPEECH_SPEED != 1.0:
                try:
                    from pydub.effects import speedup
                    segment = speedup(segment, playback_speed=SPEECH_SPEED)
                except Exception:
                    try:
                        new_frame_rate = int(segment.frame_rate * SPEECH_SPEED)
                        segment = segment._spawn(segment.raw_data, overrides={"frame_rate": new_frame_rate})
                        segment = segment.set_frame_rate(24000)
                    except Exception:
                        print("Warning: failed to apply global speed change to segment; continuing with original speed.")

            # Look ahead: ensure this segment does not overlap the next subtitle begin
            allowed_ms = None
            if index + 1 < total_lines:
                next_begin = paragraphs[index + 1].get('begin')
                if next_begin:
                    try:
                        allowed_ms = parse_ttml_timestamp(next_begin) - target_start_ms
                    except Exception:
                        allowed_ms = None
            if allowed_ms is not None and allowed_ms > 0 and len(segment) > allowed_ms:
                actual_ms = len(segment)
                factor = actual_ms / allowed_ms
                capped_factor = min(factor, 3.0)
                print(f"Segment too long ({actual_ms} ms) for allowed {allowed_ms} ms; attempting speedup x{capped_factor:.2f}.")
                try:
                    from pydub.effects import speedup
                    segment = speedup(segment, playback_speed=capped_factor)
                except Exception:
                    try:
                        new_frame_rate = int(segment.frame_rate * capped_factor)
                        segment = segment._spawn(segment.raw_data, overrides={"frame_rate": new_frame_rate})
                        segment = segment.set_frame_rate(24000)
                    except Exception:
                        print("Warning: failed to apply per-segment speedup; will trim to fit.")
                # if still too long, trim
                if len(segment) > allowed_ms:
                    print(f"Trimming segment from {len(segment)} ms to {allowed_ms} ms to avoid overlap.")
                    segment = segment[:allowed_ms]

            # Append to main track
            full_audio_track += segment
            current_track_position_ms += len(segment)

            # If requested, stop when we've reached the preview duration
            if PREVIEW_MS > 0 and current_track_position_ms >= PREVIEW_MS:
                print(f"Reached preview limit ({PREVIEW_MS} ms). Stopping early for diagnostics.")
                break
            
        except Exception as e:
            print(f"Error processing line {index + 1}: {e}")

    print("Exporting final audio file...")
    try:
        full_audio_track.export(output_path, format="mp3")
        print(f"Success! Audio saved to: {output_path}")
    except Exception as e:
        print(f"Error saving file: {e}")


def generate_audio_using_ffmpeg_concat(input_path, output_path):
    """Fallback that uses gTTS per-line + ffmpeg to create silence segments and concatenate MP3s."""
    import subprocess, tempfile

    if not os.path.exists(input_path):
        print(f"Error: Input file '{input_path}' not found.")
        return

    print("Parsing XML for ffmpeg concat fallback...")
    try:
        tree = ET.parse(input_path)
        root = tree.getroot()
    except ET.ParseError as e:
        print(f"Error parsing XML: {e}")
        return

    namespaces = {'tt': 'http://www.w3.org/ns/ttml'}
    paragraphs = root.findall('.//tt:p', namespaces)

    tmpdir = tempfile.mkdtemp(prefix='vtt_tts_')
    concat_list_path = os.path.join(tmpdir, 'concat_list.txt')
    entries = []
    current_pos = 0
    counter = 0
    failed_segments = []

    print(f"Found {len(paragraphs)} subtitle lines. Generating segments...")

    for idx, p in enumerate(paragraphs):
        text = (p.text or '').strip()
        if not text:
            continue
        begin = p.get('begin')
        if not begin:
            continue
        start_ms = parse_ttml_timestamp(begin)

        # silence needed before this segment
        silence_ms = start_ms - current_pos
        if silence_ms > 0:
            silence_s = max(0.001, silence_ms / 1000.0)
            silence_file = os.path.join(tmpdir, f"silent_{counter}.mp3")
            # Use ffmpeg to create silence with matching sample rate/mono
            cmd = [
                'ffmpeg', '-y', '-f', 'lavfi', '-i', f"anullsrc=r=24000:cl=mono",
                '-t', f"{silence_s}", '-q:a', '9', '-acodec', 'libmp3lame', silence_file
            ]
            subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            entries.append(silence_file)
            current_pos += int(silence_s * 1000)
            counter += 1
        else:
            # previous audio overlaps into this segment; try to trim previous generated segments to make room
            overlap = -silence_ms
            print(f"Detected overlap before segment #{idx+1}: need to free {overlap} ms by trimming previous segments.")
            # Find previous seg files (not silence files) and trim/remove as needed
            while overlap > 0 and entries:
                # find last seg file index
                j = None
                for k in range(len(entries)-1, -1, -1):
                    if os.path.basename(entries[k]).startswith('seg_'):
                        j = k
                        break
                if j is None:
                    break
                prev_file = entries[j]
                # get duration
                try:
                    probe = subprocess.run(['ffprobe', '-v', 'error', '-show_entries', 'format=duration', '-of', 'default=nk=1:nw=1', prev_file], capture_output=True, text=True, check=True)
                    prev_dur_s = float(probe.stdout.strip()) if probe.stdout.strip() else 0.0
                except Exception:
                    prev_dur_s = 0.0
                prev_dur_ms = int(prev_dur_s * 1000)
                if prev_dur_ms > overlap:
                    # trim previous file to (prev_dur_ms - overlap)
                    new_dur_s = (prev_dur_ms - overlap) / 1000.0
                    trimmed_file = prev_file + '.trim.mp3'
                    cmd = ['ffmpeg', '-y', '-i', prev_file, '-t', f"{new_dur_s}", '-c:a', 'libmp3lame', '-q:a', '2', trimmed_file]
                    try:
                        subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                        entries[j] = trimmed_file
                        current_pos -= overlap
                        print(f"Trimmed previous segment {os.path.basename(prev_file)} by {overlap} ms to avoid overlap.")
                        overlap = 0
                    except Exception as e:
                        print(f"Failed trimming previous segment: {e}")
                        break
                else:
                    # remove the entire previous file
                    print(f"Removing previous segment {os.path.basename(prev_file)} (duration {prev_dur_ms} ms) to free up space.")
                    try:
                        entries.pop(j)
                        current_pos -= prev_dur_ms
                        overlap -= prev_dur_ms
                    except Exception:
                        break
            if overlap > 0:
                print(f"Warning: unable to fully remove overlap ({overlap} ms remain). The following segment may start late.")

        # create tts segment with retries and better diagnostics
        seg_file = os.path.join(tmpdir, f"seg_{counter}.mp3")
        max_attempts = 3
        attempt = 0
        success = False
        import time
        while attempt < max_attempts and not success:
            attempt += 1
            try:
                tts = gTTS(text=text, lang=TTS_LANGUAGE)
                tts.save(seg_file)
                success = True
            except Exception as e:
                # Provide more diagnostics: include index, short text, exception repr
                err_repr = repr(e)
                print(f"Error generating TTS for subtitle #{idx+1} (seg {counter}) attempt {attempt}/{max_attempts}: {err_repr}")
                print(f"  Text (truncated): {text[:120]!s}")
                # Try to extract HTTP-like status if present
                try:
                    import gtts
                    from gtts.tts import gTTSError
                    if isinstance(e, gTTSError):
                        print("  Detected gTTSError (possible API/network issue).")
                except Exception:
                    pass
                if attempt < max_attempts:
                    delay = 2 ** (attempt - 1)
                    print(f"  Retrying in {delay}s...")
                    time.sleep(delay)
                else:
                    print(f"  Final failure for subtitle #{idx+1}; skipping segment.")
                    failed_segments.append((idx+1, text, err_repr))
        if not success:
            # Skip adding this segment and continue main loop
            continue

        # measure segment duration via ffprobe/ffmpeg
        try:
            probe = subprocess.run(['ffprobe', '-v', 'error', '-show_entries', 'format=duration', '-of', 'default=nk=1:nw=1', seg_file], capture_output=True, text=True, check=True)
            dur_s = float(probe.stdout.strip()) if probe.stdout.strip() else 0.0
        except Exception:
            dur_s = 0.0

        # Check whether this segment would overlap the next subtitle and adjust
        allowed_ms = None
        if idx + 1 < len(paragraphs):
            next_begin = paragraphs[idx + 1].get('begin')
            if next_begin:
                try:
                    allowed_ms = parse_ttml_timestamp(next_begin) - start_ms
                except Exception:
                    allowed_ms = None
        dur_ms = int(dur_s * 1000)
        if allowed_ms is not None and allowed_ms > 0 and dur_ms > allowed_ms:
            factor = dur_ms / allowed_ms
            capped_factor = min(factor, 3.0)
            print(f"Segment {counter} too long ({dur_ms} ms) for allowed {allowed_ms} ms; attempting ffmpeg speedup x{capped_factor:.2f}.")
            # build atempo chain for capped_factor
            speed = capped_factor
            if 0.5 <= speed <= 2.0:
                filter_spec = f"atempo={speed}"
            else:
                parts = []
                remaining = speed
                while remaining > 2.0:
                    parts.append("atempo=2.0")
                    remaining /= 2.0
                while remaining < 0.5:
                    parts.append("atempo=0.5")
                    remaining /= 0.5
                parts.append(f"atempo={remaining}")
                filter_spec = ",".join(parts)
            sped_file = seg_file + '.sped.mp3'
            cmd = ['ffmpeg', '-y', '-i', seg_file, '-filter:a', filter_spec, '-c:a', 'libmp3lame', '-q:a', '2', sped_file]
            try:
                subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                # re-probe
                probe = subprocess.run(['ffprobe', '-v', 'error', '-show_entries', 'format=duration', '-of', 'default=nk=1:nw=1', sped_file], capture_output=True, text=True, check=True)
                new_dur_s = float(probe.stdout.strip()) if probe.stdout.strip() else 0.0
                new_dur_ms = int(new_dur_s * 1000)
                print(f"  Sped segment duration: {new_dur_ms} ms")
                if new_dur_ms <= allowed_ms:
                    seg_file = sped_file
                    dur_ms = new_dur_ms
                else:
                    # if still too long, trim
                    print(f"  Still too long after speedup; trimming to {allowed_ms} ms")
                    trimmed_file = seg_file + '.trim.mp3'
                    cmd = ['ffmpeg', '-y', '-i', sped_file, '-t', f"{allowed_ms/1000.0}", '-c:a', 'libmp3lame', '-q:a', '2', trimmed_file]
                    subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                    seg_file = trimmed_file
                    dur_ms = allowed_ms
            except Exception as e:
                print(f"Failed to speed/trim segment {counter}: {e}; leaving original segment (may overlap).")
        entries.append(seg_file)
        current_pos += dur_ms
        counter += 1

        # respect preview cutoff
        if PREVIEW_MS > 0 and current_pos >= PREVIEW_MS:
            print(f"Reached preview limit ({PREVIEW_MS} ms) during generation. Stopping early.")
            break

    if not entries:
        print("No entries to concatenate.")
        return

    # write concat list
    with open(concat_list_path, 'w', encoding='utf-8') as f:
        for p in entries:
            f.write(f"file '{p.replace("'","'\''")}'\n")

    # concatenate and re-encode (safer than stream copy; avoids DTS issues)
    # SPEECH_SPEED is guaranteed to be >= 2.0 by earlier validation; use it directly
    speed = SPEECH_SPEED
    # Build atempo filter. atempo supports 0.5-2.0 per instance, chain if necessary.
    if 0.5 <= speed <= 2.0:
        filter_spec = f"atempo={speed}"
    else:
        parts = []
        remaining = speed
        while remaining > 2.0:
            parts.append("atempo=2.0")
            remaining /= 2.0
        while remaining < 0.5:
            parts.append("atempo=0.5")
            remaining /= 0.5
        parts.append(f"atempo={remaining}")
        filter_spec = ",".join(parts)
    cmd = ['ffmpeg', '-y', '-f', 'concat', '-safe', '0', '-i', concat_list_path, '-c:a', 'libmp3lame', '-q:a', '2', '-filter:a', filter_spec, output_path]
    print(f"Applying speed filter: {filter_spec}")
    try:
        subprocess.run(cmd, check=True)
        print(f"Success! Audio saved to: {output_path}")
    except subprocess.CalledProcessError as e:
        print(f"ffmpeg concat failed: {e}")
    finally:
        # cleanup temporary files
        try:
            import shutil
            shutil.rmtree(tmpdir)
        except Exception:
            pass

    # Print summary of any TTS failures
    if failed_segments:
        print('\nTTS generation failed for {} segments:'.format(len(failed_segments)))
        for i, txt, err in failed_segments[:10]:
            print(f" - subtitle #{i}: {err} (text starts: {txt[:80]!s})")
        if len(failed_segments) > 10:
            print(f" - ...and {len(failed_segments)-10} more failures.")
        print("If failures are due to transient network/API errors we can retry the whole run, or you can increase retries.")

def generate_gtts_preview(input_path, output_path="diag_preview.mp3"):
    if not os.path.exists(input_path):
        print(f"Error: Input file '{input_path}' not found.")
        return
    try:
        tree = ET.parse(input_path)
        root = tree.getroot()
    except ET.ParseError as e:
        print(f"Error parsing XML: {e}")
        return
    namespaces = {'tt': 'http://www.w3.org/ns/ttml'}
    paragraphs = root.findall('.//tt:p', namespaces)
    for p in paragraphs:
        text = (p.text or "").strip()
        if text:
            print(f"Using first subtitle for preview: {text[:60]}...")
            try:
                tts = gTTS(text=text, lang=TTS_LANGUAGE)
                tts.save(output_path)
                print(f"Preview saved to: {output_path}")
            except Exception as e:
                print(f"Error generating preview: {e}")
            return
    print("No subtitle lines found for preview.")

if __name__ == "__main__":
    # create timestamped output filename to avoid overwriting previous runs
    ts = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    out_name = f"generated_audio_{ts}.mp3"

    if DIAG_GTTSSAMPLE:
        generate_gtts_preview(INPUT_FILENAME)
    else:
        if USE_PYDUB:
            print(f"Writing: {out_name}")
            generate_audio_from_subtitles(INPUT_FILENAME, out_name)
        else:
            print("pydub unavailable — using ffmpeg-only concatenation fallback.")
            print(f"Writing: {out_name}")
            generate_audio_using_ffmpeg_concat(INPUT_FILENAME, out_name)