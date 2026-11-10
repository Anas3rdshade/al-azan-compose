package com.github.meypod.al_azan.main.namaz

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HowToPrayStepsTest {
    private fun count(p: Prayer, pose: Pose, mode: Mode = Mode.STANDING) = buildSteps(p, false, mode).count { it.pose == pose }

    @Test
    fun rukuPerRakat() {
        assertEquals(2, count(Prayer.FAJR, Pose.RUKU))
        assertEquals(3, count(Prayer.MAGHRIB, Pose.RUKU))
        assertEquals(4, count(Prayer.ISHA, Pose.RUKU, Mode.CHAIR))
        assertEquals(0, count(Prayer.JANAZAH, Pose.RUKU))
        assertEquals(0, count(Prayer.JANAZAH, Pose.SAJDAH))
    }

    @Test
    fun sittingsAndSalam() {
        assertEquals(1, count(Prayer.FAJR, Pose.QADAH))
        assertEquals(2, count(Prayer.DHUHR, Pose.QADAH))
        // Janazah ends with its own standing salam figures, right then left, and never a seated one.
        val janazah = buildSteps(Prayer.JANAZAH, true, Mode.STANDING)
        assertEquals(listOf(Pose.J_SALAM_R, Pose.J_SALAM_L), janazah.takeLast(2).map { it.pose })
        assertTrue(janazah.none { it.pose in setOf(Pose.JALSA, Pose.QADAH, Pose.SALAM_R, Pose.SALAM_L) })
    }

    @Test
    fun everyPrayerStartsWithNiyyahAndEndsWithSalam() {
        for (mode in Mode.entries) for (p in Prayer.entries) for (w in listOf(false, true)) {
            val steps = buildSteps(p, w, mode)
            assertEquals(Pose.NIYYAH, steps.first().pose)
            assertEquals(2, steps.takeLast(2).count { it.pose in setOf(Pose.SALAM_R, Pose.SALAM_L, Pose.J_SALAM_R, Pose.J_SALAM_L) })
        }
    }

    @Test
    fun specialSteps() {
        val witr = buildSteps(Prayer.WITR, false, Mode.STANDING)
        assertTrue(witr.any { it.title == com.github.meypod.al_azan.R.string.sg_qunoot_t && it.rakat == 3 })
        // Witr reads a surah in all 3 rakats; Maghrib only in the first 2.
        assertEquals(3, witr.count { it.title == com.github.meypod.al_azan.R.string.sg_surah_t })
        assertEquals(2, buildSteps(Prayer.MAGHRIB, false, Mode.STANDING).count { it.title == com.github.meypod.al_azan.R.string.sg_surah_t })
        assertEquals(2, buildSteps(Prayer.EID, false, Mode.STANDING).count { it.title == com.github.meypod.al_azan.R.string.sg_eid_t })
        // Janazah has no rakats, so its niyyah must not mention them.
        assertEquals(com.github.meypod.al_azan.R.string.sg_niyyah_janazah, buildSteps(Prayer.JANAZAH, false, Mode.STANDING).first().body)
    }

    @Test
    fun seatedNeverSaysStandUp() {
        val standUp = setOf(com.github.meypod.al_azan.R.string.sg_fatiha_next, com.github.meypod.al_azan.R.string.sg_fatiha_only)
        assertTrue(buildSteps(Prayer.DHUHR, true, Mode.CHAIR).none { it.body in standUp })
        assertTrue(buildSteps(Prayer.DHUHR, false, Mode.STANDING).any { it.body in standUp })
    }
}
