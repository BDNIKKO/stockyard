"""Generate the presentation voice-over with Kokoro (open-source neural text-to-speech, runs locally).

The narration text lives in docs/presentation/index.html inside <script id="narration" type="application/json">.
One MP3 is produced per slide step: assets/vo/<slide>_<step>.mp3.

    python -m venv venv && venv/Scripts/pip install -r tools/voiceover/requirements.txt
    # download kokoro-v1.0.onnx and voices-v1.0.bin from
    # https://github.com/thewh1teagle/kokoro-onnx/releases/tag/model-files-v1.0
    venv/Scripts/python tools/voiceover/generate_voiceover.py --models <folder with the two files>
    venv/Scripts/python tools/voiceover/generate_voiceover.py --models <folder> --voice am_michael --speed 0.97
"""
import argparse
import json
import re
from pathlib import Path

import lameenc
import numpy as np
from kokoro_onnx import Kokoro

ROOT = Path(__file__).resolve().parents[2]
DECK = ROOT / "docs" / "presentation" / "index.html"


def load_narration() -> dict:
    html = DECK.read_text(encoding="utf-8")
    block = re.search(r'<script id="narration" type="application/json">(.*?)</script>', html, re.S)
    if not block:
        raise SystemExit("narration block not found in the deck")
    return json.loads(block.group(1))


def finish(samples: np.ndarray, sr: int) -> np.ndarray:
    """Consistent loudness across clips, with short fades so clips never click."""
    rms = float(np.sqrt(np.mean(samples ** 2))) or 1.0
    samples = samples * (0.10 / rms)                      # about -20 dBFS RMS
    peak = float(np.max(np.abs(samples))) or 1.0
    if peak > 0.95:
        samples = samples * (0.95 / peak)
    fade = int(sr * 0.03)
    samples[:fade] *= np.linspace(0, 1, fade)
    samples[-fade:] *= np.linspace(1, 0, fade)
    return samples


def to_mp3(samples: np.ndarray, sr: int) -> bytes:
    pcm = (np.clip(samples, -1, 1) * 32767).astype(np.int16)
    enc = lameenc.Encoder()
    enc.set_bit_rate(64)
    enc.set_in_sample_rate(sr)
    enc.set_channels(1)
    enc.set_quality(2)
    return bytes(enc.encode(pcm.tobytes()) + enc.flush())


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--models", required=True, help="folder containing kokoro-v1.0.onnx and voices-v1.0.bin")
    ap.add_argument("--voice", default="af_heart")
    ap.add_argument("--speed", type=float, default=1.0)
    ap.add_argument("--out", default=str(DECK.parent / "assets" / "vo"))
    args = ap.parse_args()

    models = Path(args.models)
    kokoro = Kokoro(str(models / "kokoro-v1.0.onnx"), str(models / "voices-v1.0.bin"))
    lang = "en-gb" if args.voice.startswith("b") else "en-us"
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)

    total = 0.0
    narration = load_narration()
    for key, text in narration.items():
        samples, sr = kokoro.create(text, voice=args.voice, speed=args.speed, lang=lang)
        samples = finish(samples.astype(np.float64), sr)
        (out / f"{key}.mp3").write_bytes(to_mp3(samples, sr))
        secs = len(samples) / sr
        total += secs
        print(f"{key:>5}  {secs:5.1f}s  {text[:60]}")
    print(f"\n{len(narration)} clips, {total / 60:.1f} minutes of narration -> {out}")


if __name__ == "__main__":
    main()
