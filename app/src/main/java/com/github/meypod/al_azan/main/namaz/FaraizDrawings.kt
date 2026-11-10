package com.github.meypod.al_azan.main.namaz

import androidx.annotation.DrawableRes
import com.github.meypod.al_azan.R

/** Faraiz pictures. Steps: Gemini close-ups (Namaz Tracker/10_Faraiz_Illustrations_Plan.md); positions reuse the women's salah guide poses. */
internal enum class Pic(@DrawableRes val res: Int) {
    None(0),
    Jug(R.drawable.faraiz_jug), Hands(R.drawable.faraiz_hands), Mouth(R.drawable.faraiz_mouth), Nose(R.drawable.faraiz_nose),
    Face(R.drawable.faraiz_face), Arm(R.drawable.faraiz_arm), HeadTop(R.drawable.faraiz_headtop), Ears(R.drawable.faraiz_ears),
    Foot(R.drawable.faraiz_foot), Khilal(R.drawable.faraiz_khilal), Body(R.drawable.faraiz_body), DontMiss(R.drawable.faraiz_dontmiss),
    Strike(R.drawable.faraiz_strike), Strike2(R.drawable.faraiz_strike2),
    Takbeer(R.drawable.salah_women_takbir), Qiyam(R.drawable.salah_women_qiyam), Ruku(R.drawable.salah_women_ruku),
    Qawma(R.drawable.salah_women_qawmah), Sajda(R.drawable.salah_women_sajdah), Jalsa(R.drawable.salah_women_jalsa),
    Qada(R.drawable.salah_women_qadah), Salam(R.drawable.salah_women_salam_right),
}
