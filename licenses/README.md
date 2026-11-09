# Bundled fonts

All fonts are licensed under the SIL Open Font License 1.1 (full text alongside this file) and
shipped in `app/src/main/res/font/`. They are applied per UI locale in `core/presentation/Theme.kt`.

## Vazirmatn — Persian (`fa`)

- File: `app/src/main/res/font/vazirmatn.ttf` (variable, `wght` axis)
- Source: <https://github.com/google/fonts/tree/main/ofl/vazirmatn>
- License: [Vazirmatn-OFL.txt](Vazirmatn-OFL.txt)
- **Unmodified.**

## Noto Sans Arabic — Arabic (`ar`)

- File: `app/src/main/res/font/noto_sans_arabic.ttf` (variable, `wght` axis)
- Source: <https://github.com/google/fonts/tree/main/ofl/notosansarabic>
- License: [NotoSansArabic-OFL.txt](NotoSansArabic-OFL.txt)
- **Modified.** Its line-spacing metrics were reduced from ~2.112em to ~1.562em per line so Arabic
  UI text matches the line height of Vazirmatn (Persian) and the Latin default; upstream reserves
  the extra space for stacked harakat, which this app does not render. Only line metrics were
  touched — `hhea` ascent/descent/lineGap and `OS/2.sTypo*` (ascent 1025, descent -537, gap 0);
  glyphs are untouched. Reproduce with [`tools/patch_noto_arabic_metrics.py`](../tools/patch_noto_arabic_metrics.py).
  The OFL declares no Reserved Font Name, so the modification keeps the original name.

## Manrope — UI (all other locales)

- File: `app/src/main/res/font/manrope.ttf` (variable, `wght` axis)
- Source: <https://github.com/google/fonts/tree/main/ofl/manrope>
- License: [Manrope-OFL.txt](Manrope-OFL.txt)
- **Unmodified.**

## Amiri — Arabic display text (Hijri date, prayer names on the Today screen)

- Files: `app/src/main/res/font/amiri_regular.ttf`, `app/src/main/res/font/amiri_bold.ttf`
- Source: <https://github.com/google/fonts/tree/main/ofl/amiri>
- License: [Amiri-OFL.txt](Amiri-OFL.txt)
- **Unmodified.**
