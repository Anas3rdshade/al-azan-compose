# Namaz Tracker — content sources and licences

All checked live on 2026-11-09. "Bundle?" means: may this file ship inside a sideloaded AGPL-3.0 APK (and its public GitHub fork) as a separate aggregate work under AGPL §5? The content keeps its own licence; the AGPL does not cover it.

`source_id` values below are the ones used in `content/**/*.json`.

## Quran text

### `tanzil_uthmani` — Tanzil Quran Text (Uthmani), v1.1
- URL: https://tanzil.net/docs/text_license (the same notice is inside the download)
- Licence quote: "Tanzil Quran Text Copyright (C) 2007-2026 Tanzil Project License: Creative Commons Attribution 3.0 … Permission is granted to copy and distribute verbatim copies of this text, but CHANGING IT IS NOT ALLOWED. This Quran text can be used in any website or application, provided that its source (Tanzil Project) is clearly indicated, and a link is made to tanzil.net … This copyright notice shall be included in all verbatim copies"
- Bundle? **Yes.**
- Conditions: keep the text verbatim, with no re-encoding or "fixing" of marks; put the full notice in the assets; credit and link tanzil.net on the About screen.
- Verdict: **USE.**

## Quran translations

### `quranenc_hindi_omari` — Hindi, Azizul Haq Al-Omari, QuranEnc v1.1.5
- URL: https://quranenc.com/en/browse/hindi_omari (terms in the page footer); API https://quranenc.com/api/v1/translation/sura/hindi_omari/1
- Licence quote: "Contents of the translations can be downloaded and re-published, with the following terms and conditions: 1. No modification, addition, or deletion of the content. 2. Clearly referring to the publisher and the source (QuranEnc.com). 3. Mentioning the version number when re-publishing the translation. 4. Keeping the transcript information inside the document. 5. Notifying the source (QuranEnc.com) of any note on the translation. 6. Updating the translation according to the latest version issued from the source (QuranEnc.com). 7. Inappropriate advertisements must not be included …"
- Bundle? **Yes.** There is no non-commercial limit.
- Conditions: verbatim, footnotes kept, version stored; credit QuranEnc.com; refresh when a new version appears.
- Verdict: **USE** (Hindi).

### `quranenc_urdu_junagarhi` — Urdu, Muhammad Ibrahim Junagarhi, QuranEnc v1.1.3
- URL: https://quranenc.com/en/browse/urdu_junagarhi
- Licence: the same 7 QuranEnc terms quoted above.
- Bundle? **Yes.**
- Note: the text uses the "ﻻ" presentation-form ligature. It must be shown as-is, not normalised, because term 1 forbids modification.
- Verdict: **USE** (Urdu).

### Tanzil translations (hi.farooq, hi.hindi, ur.jalandhry, ur.junagarhi, ur.maududi …) — fallback only
- URL: https://tanzil.net/trans/
- Licence quote: "The translations provided at this page are for non-commercial purposes only. If used otherwise, you need to obtain necessary permission from the translator or the publisher."
- Bundle? **Legally yes for a free app, but weaker.** A public AGPL repo lets anyone reuse it commercially, which this licence forbids. hi.farooq also has a grammar slip in 1:1 ("हैं" where "है" is correct).
- Verdict: **DO NOT USE.** QuranEnc covers both languages with better terms.

## Quran audio

### `islamicnetwork_alafasy` — Mishary Alafasy 128 kbps, Islamic Network CDN
- URL: https://alquran.cloud/terms-and-conditions §IV; files at `https://cdn.islamic.network/quran/audio/128/ar.alafasy/{globalAyah}.mp3`
- Licence quote: "Recitations are licensed to us by the reciters or their estates for free, non-commercial redistribution at the bitrates we publish. You may stream, embed and download them for personal and educational use. You may bundle them into a commercial product, but please note that copyrights lie with the reciters and they may ask you to remove the conent." Also: "All audio files used on this website and third party libraries own and retain their respective copyrights."
- Bundle? **Yes**, with attribution.
- Risk: the reciter keeps copyright and may ask for removal. If that happens, drop the files.
- Verdict: **USE.**
- Downloaded: `audio/quran/ar.alafasy/001_001.mp3` to `001_007.mp3`, 796,680 B in total. Each file starts with a valid MP3 frame header.

### everyayah.com
- No licence or terms page found on the site or in searches.
- Verdict: **DO NOT BUNDLE.** Silence is not a grant.

### QuranicAudio (https://www.quranicaudio.com/about)
- Quote: "…may be downloaded and used for personal use free of charge. However, you may not use these files for commercial purposes…"
- The files are gathered from many third parties.
- Verdict: **DO NOT BUNDLE.** It is personal use only, and redistribution is not granted.

## Duas (Hisnul Muslim subset)

### `houseofislam_duas` — House of Islam for Developers, duas bundle (491 entries, English only)
- URL: https://developers.thehouseofislam.com/terms
- Download: `https://firebasestorage.googleapis.com/v0/b/qibla-app-1f18c.appspot.com/o/api%2Fpublic-downloads%2Fduas.zip?alt=media`
- Licence quote: "provided free of charge, for personal and commercial use alike … Attribution. Not required, but always appreciated". It also says: "Content is compiled from a range of sources and may contain errors — verify anything … worship-critical against a trusted scholar".
- Bundle? **Yes** for Arabic, transliteration and hadith references.
- Risk: it is unclear whether they hold the right to license the compiled transliteration. The Arabic hadith wording itself is 1,400-year-old public-domain text.
- Verdict: **USE** for Arabic, transliteration and references. Check the Arabic against a printed Hisnul Muslim.

### hisnmuslim.com — Hisnul Muslim in Hindi (https://hisnmuslim.com/i/hi/1), no Urdu
- **A Hindi Hisnul Muslim does exist** (for example chapter i/hi/27, "सुबह और शाम के अज़कार").
- The site has no licence, terms or copyright line, and its footer is empty.
- Verdict: **NOT WITHOUT WRITTEN PERMISSION.** Email the site before using it. This is the best Hindi option if they agree.

### thenasihun/me-azkar-web — GitHub, MIT, "Copyright (c) 2025 Nasihun"
- URL: https://github.com/thenasihun/me-azkar-web
- Contents: 30 morning/evening azkar with Arabic, Urdu, English and audio.
- Bundle? **The MIT licence allows it.**
- Risk: the Urdu may have been copied from a published translation, and the repo cannot license what it does not own.
- Verdict: **CAUTION.** Use only after spot-checking the Urdu against known print translations. It is not used in the sample.

### rn0x/hisn_almuslim_json — GitHub, licence: none
- Verdict: **DO NOT USE.** A repo with no licence grants nothing.

### Others found but not inspected
- muslimpack/Al-Azkar: GPL-3.0, Arabic only (Nawawi's al-Adhkar).
- my-prayers/muslim-data-android: Apache-2.0, azkar in a database.
- YousefAsalya/Islamic-Pro-azkar-API: MIT; Arabic, English and Turkish only.

### `own_translation` — Hindi and Urdu meanings written for this app
- These are our own work, so we hold the rights.
- Status: `draft_needs_native_reader`. A native Hindi/Urdu reader, and ideally the local imam, must check them before release.
- The Hindi uses a mixed "माँगता/माँगती" form, which the app could later switch by user gender.
- Verdict: **USE after review.** This is the fallback for every dua Hindi and Urdu line.

## Attribution block for the About screen
> Quran text: Tanzil Project (tanzil.net), CC BY 3.0, verbatim. Hindi translation: Azizul Haq Al-Omari, QuranEnc.com v1.1.5. Urdu translation: Muhammad Ibrahim Junagarhi, QuranEnc.com v1.1.3. Recitation: Mishary Rashid Alafasy via Islamic Network (alquran.cloud). Dua Arabic and transliteration: House of Islam for Developers. Dua Hindi/Urdu meanings: written for this app.
